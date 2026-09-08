# Modelagem

## Diagrama de classes de domínio

```mermaid
classDiagram
    class User {
        -Long id
        -String name
        -String email
        -String passwordHash
        -Set~Role~ roles
        +isAdmin() boolean
    }

    class Role {
        <<enumeration>>
        ROLE_CUSTOMER
        ROLE_ADMIN
    }

    class Category {
        -Long id
        -String name
        -String slug
        +setName(String)
    }

    class Product {
        -Long id
        -String sku
        -String name
        -BigDecimal price
        -int stockQuantity
        -boolean active
        -Long version
        +hasStock(int) boolean
        +removeFromStock(int)
        +returnToStock(int)
        +deactivate()
    }

    class Order {
        -Long id
        -String code
        -OrderStatus status
        -BigDecimal shippingFee
        +addItem(Product, int)
        +getItemsTotal() BigDecimal
        +getTotal() BigDecimal
        +pay(Payment)
        +ship()
        +deliver()
        +cancel()
        -transitionTo(OrderStatus)
    }

    class OrderItem {
        -Long id
        -String productName
        -String productSku
        -int quantity
        -BigDecimal unitPrice
        +getSubtotal() BigDecimal
    }

    class OrderStatus {
        <<enumeration>>
        PENDING_PAYMENT
        PAID
        SHIPPED
        DELIVERED
        CANCELED
        +canTransitionTo(OrderStatus) boolean
        +holdsStock() boolean
    }

    class Address {
        <<embeddable>>
        -String street
        -String number
        -String city
        -String state
        -String zipCode
    }

    class Payment {
        <<abstract>>
        -Long id
        -Instant paidAt
        +getMethod()* PaymentMethod
        +getDescription()* String
        +attachTo(Order)
    }

    class CreditCardPayment {
        -String cardBrand
        -String cardLast4
        -Integer installments
        +getMethod() PaymentMethod
        +getDescription() String
    }

    class PixPayment {
        -String txid
        +getMethod() PaymentMethod
        +getDescription() String
    }

    class BoletoPayment {
        -String barcode
        -LocalDate dueDate
        +getMethod() PaymentMethod
        +getDescription() String
    }

    User "1" --> "*" Role : possui
    Category "1" --> "*" Product : classifica
    User "1" --> "*" Order : realiza
    Order "1" *-- "1..*" OrderItem : contém
    Order "1" *-- "0..1" Payment : é quitado por
    Order "1" o-- "1" Address : entrega em
    Order ..> OrderStatus : usa
    OrderItem "*" --> "1" Product : referencia
    Payment <|-- CreditCardPayment
    Payment <|-- PixPayment
    Payment <|-- BoletoPayment
```

`Order` é a raiz do agregado: `OrderItem` e `Payment` só nascem e morrem através dela.
Nenhuma camada externa consegue instanciar um `OrderItem` solto — o construtor é
package-private de propósito.

## Máquina de estados do pedido

```mermaid
stateDiagram-v2
    [*] --> PENDING_PAYMENT : POST /orders (reserva estoque)
    PENDING_PAYMENT --> PAID : POST /orders/{id}/payment
    PENDING_PAYMENT --> CANCELED : POST /orders/{id}/cancellation
    PAID --> SHIPPED : POST /orders/{id}/shipment (ADMIN)
    PAID --> CANCELED : POST /orders/{id}/cancellation
    SHIPPED --> DELIVERED : POST /orders/{id}/delivery (ADMIN)
    DELIVERED --> [*]
    CANCELED --> [*]

    note right of CANCELED
        Só PENDING_PAYMENT e PAID
        seguram estoque reservado.
        O cancelamento devolve ao
        estoque apenas a partir
        desses dois estados.
    end note
```

O grafo de transições vive dentro do enum `OrderStatus`, não espalhado em `if`s
pelo service. Adicionar um estado novo (`REFUNDED`, por exemplo) é mexer em um
arquivo só, e os testes parametrizados de `OrderStatusTest` cobrem a matriz inteira.

## Sequência do fechamento de pedido

```mermaid
sequenceDiagram
    autonumber
    actor C as Cliente
    participant API as OrderController
    participant S as OrderService
    participant PR as ProductRepository
    participant O as Order (agregado)
    participant DB as PostgreSQL

    C->>API: POST /api/v1/orders (itens + endereço)
    API->>S: create(email, request)
    Note over S: Consolida quantidades por produto<br/>antes de tocar no estoque
    S->>DB: BEGIN
    loop para cada produto distinto
        S->>PR: findByIdForUpdate(productId)
        PR->>DB: SELECT ... FOR UPDATE
        DB-->>PR: linha travada
        S->>O: removeFromStock(qtd)
        alt estoque insuficiente
            O-->>S: BusinessException
            S->>DB: ROLLBACK
            API-->>C: 422 Unprocessable Entity
        end
        S->>O: addItem(produto, qtd)
    end
    S->>DB: INSERT orders + order_items, COMMIT
    API-->>C: 201 Created + Location
```

O `SELECT ... FOR UPDATE` serializa duas compras simultâneas do mesmo produto.
Sem ele, dois pedidos concorrentes leem `stock = 1` e ambos passam na validação —
o clássico problema de _lost update_ que só aparece em produção, nunca no teste manual.

## Modelo físico

```mermaid
erDiagram
    users ||--o{ user_roles : tem
    users ||--o{ orders : realiza
    categories ||--o{ products : classifica
    orders ||--|{ order_items : contem
    orders ||--o| payments : quitado_por
    products ||--o{ order_items : referenciado_em

    users {
        bigserial id PK
        varchar email UK
        varchar password_hash
    }
    products {
        bigserial id PK
        varchar sku UK
        numeric price
        int stock_quantity
        bigint version
        bigint category_id FK
    }
    orders {
        bigserial id PK
        varchar code UK
        varchar status
        bigint user_id FK
    }
    order_items {
        bigserial id PK
        bigint order_id FK
        bigint product_id FK
        int quantity
        numeric unit_price
    }
    payments {
        bigserial id PK
        varchar payment_type
        bigint order_id UK
    }
```

**Herança `payments`:** estratégia `SINGLE_TABLE` discriminada por `payment_type`.
Troca espaço (colunas nulas para os tipos que não as usam) por consultas polimórficas
sem `join`, que é o padrão de acesso deste domínio. `JOINED` faria sentido se os
meios de pagamento tivessem muitos campos próprios ou se houvesse dezenas deles.

# ms-digitalfix-report

**Proyecto:** DigitalFix - Gestion de ordenes de trabajo para una red de 20 empresas de mantenimiento electrico  
**Componente:** Microservicio  
**Asignatura:** DSY1107 Desarrollo Cloud Native I - Duoc UC  
**Primera entrega (EP1):** 14 de septiembre de 2026

## Descripcion

Reporteria: indicadores de la red construidos a partir de eventos, sin tocar la base transaccional.

## Tecnologias

Spring Boot 3, RabbitMQ, Spring Data JPA, Oracle

## API (solo ADMIN, siempre de su empresa)

| Ruta | Que devuelve |
|---|---|
| `GET /api/reports/indicators?periodo=24h\|7d\|30d` | Total de ordenes, tiempo medio de resolucion (min) y conteo por estado |
| `GET /api/reports/top-services?periodo=` | Servicios mas solicitados |
| `GET /api/reports/orders-by-hour?periodo=` | Ordenes por hora del dia (0 a 23), siempre las 24 horas |

## Mensajeria (RabbitMQ)

`ordenes.negocio.exchange` --`ordenes.eventos.*`--> `ordenes.reportes.queue` (DLQ
`ordenes.reportes.dlq`). Nombres en `application.yml` (`amqp.*`), beans en
`config/RabbitMQConfig`, consumidor en `mensajeria.ordenes.OrdenEventoConsumer`.
El consumidor solo lee el mensaje; la proyeccion (`service.ProyeccionDeOrdenes`)
mantiene la tabla `ordenes_reporte`.

| Situacion | Decision (`mensajeria.ProcesadorDeMensajes`) |
|---|---|
| Se aplico el evento | `ACK` |
| Mensaje invalido (JSON ilegible, sin orden, tipo o empresa) | `NACK` sin reencolar: a la DLQ de inmediato |
| Falla transitoria (base de datos o red) | Hasta `amqp.reintentos.maximo` intentos (1 s, 2 s, 4 s...), luego a la DLQ |
| Otro error | `NACK` a la DLQ sin reintentar |

## Pruebas

`./mvnw test` (H2 en memoria): eventos con el formato real de workorders de
punta a punta, aislamiento por empresa, ordenes por hora, acceso solo ADMIN y la
politica de ACK/reintento/DLQ.

## Integrantes

| Integrante | GitHub |
|---|---|
| Kevin Rojas | @khrojasdev |
| Christopher Perez | @ChrisPerezV |
| Diego Lopez | @DiegoLopez-f |

## Repositorios del proyecto

- [`digitalfix-frontend`](https://github.com/khrojasdev/digitalfix-frontend) - Frontend
- [`ms-digitalfix-bff`](https://github.com/khrojasdev/ms-digitalfix-bff) - BFF / API interna
- [`ms-digitalfix-usuarios`](https://github.com/khrojasdev/ms-digitalfix-usuarios) - Microservicio
- [`ms-digitalfix-catalog`](https://github.com/khrojasdev/ms-digitalfix-catalog) - Microservicio
- [`ms-digitalfix-workorders`](https://github.com/khrojasdev/ms-digitalfix-workorders) - Microservicio
- [`ms-digitalfix-notify`](https://github.com/khrojasdev/ms-digitalfix-notify) - Microservicio
- [`ms-digitalfix-report`](https://github.com/khrojasdev/ms-digitalfix-report) - Microservicio **(este)**
- [`ms-digitalfix-audit`](https://github.com/khrojasdev/ms-digitalfix-audit) - Microservicio
- [`digitalfix-infra`](https://github.com/khrojasdev/digitalfix-infra) - Infraestructura


## Tablero

El backlog completo vive en un unico GitHub Project que enlaza los ocho repositorios.
Columnas: Backlog, To Do, In Progress, In Review, Done. Limite de trabajo en curso: 2 tarjetas por persona.

## Como se trabaja aqui

1. Cada tarea del tablero tiene su propia rama, indicada en el cuerpo del issue.
2. `git switch <rama>` - nunca se trabaja directamente sobre `main`.
3. Commits con Conventional Commits: `feat(catalogo): agrega endpoint de servicios`.
4. Pull request hacia `main` con `Closes #<numero del issue>` en la descripcion.
5. Revisa un companero distinto del autor. Recien ahi se hace merge.

Referencias entre repositorios: `khrojasdev/otro-repo#12`.

## Configuracion

Ninguna credencial vive en este repositorio. Todo llega por variables de entorno;
revisa `.env.example` para saber cuales.

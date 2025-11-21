# :book: LoanRanger

![Java](https://img.shields.io/badge/Java-24-blue)![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-brightgreen)![Database](https://img.shields.io/badge/Database-PostgreSQL-blue)![Build](https://img.shields.io/badge/Build-Maven-red)![License](https://img.shields.io/badge/License-MIT-lightgrey)

**LoanRanger** è un'applicazione gestionale per biblioteche, sviluppata come progetto per il corso di **Ingegneria del Software** dell'**Università degli Studi di Firenze**.

Il software simula la gestione di una rete bibliotecaria, regolando la circolazione dei libri e le interazioni tra diverse tipologie di utenti. L'interazione con il sistema avviene tramite un'interfaccia a riga di comando (CLI).

## Struttura del progetto

```
├── docs
│   ├── img
│   ├── test_results
│   └── uml
├── src
│   ├── main
│   │   ├── java
│   │   │   └── dev
│   │   │       └── ltocca
│   │   │           └── loanranger
│   │   │               ├── businessLogic
│   │   │               │   └── observer
│   │   │               ├── domainModel
│   │   │               │   └── State
│   │   │               ├── ORM
│   │   │               │   └── DAOInterfaces
│   │   │               ├── presentationLayer
│   │   │               ├── service
│   │   │               │   └── strategy
│   │   │               └── util
│   │   └── resources
│   │       └── sql
│   └── test
│       ├── java
│       │   └── dev
│       │       └── ltocca
│       │           └── loanranger
│       │               ├── businessLogic
│       │               ├── ORM
│       │               ├── presentationLayer
│       │               ├── service
│       │               └── util
│       └── resources
│           └── sql
```

# SQL pour créer les tables de la base de données
```sql
-- Table Menu
create table Menu
(
    jour   enum ('lundi', 'mardi', 'mercredi', 'jeudi', 'vendredi', 'samedi', 'dimanche') not null,
    moment enum ('midi', 'soir')                                                          not null,
    entree varchar(60)                                                                    null,
    plat   varchar(60)                                                                    not null,
    primary key (jour, moment)
);

-- Table Produits
create table Produits
(
    id              int auto_increment
        primary key,
    nomProduit      varchar(60)                                                    not null,
    typeProduit     enum ('entree', 'viande', 'platComplet', 'feculent', 'legume') not null,
    dateLastUsed    date                                                           null,
    poidsArbitraire int                                                            not null,
    constraint nomProduit
        unique (nomProduit)
);

-- Table PoidsSaison
create table PoidsSaison
(
    idProduit      int not null
        primary key,
    poidsAutomne   int not null,
    poidsPrintemps int not null,
    poidsEte       int not null,
    poidsHiver     int not null,
    constraint PoidsSaison_Produits_id_fk
        foreign key (idProduit) references Produits (id)
);

-- Table PoidsMoment
create table PoidsMoment
(
    idProduit        int                                                            not null
        primary key,
    typeProduit      enum ('viande', 'entree', 'feculent', 'legume', 'platComplet') not null,
    poidsMidiSemaine int                                                            not null,
    poidsSoirSemaine int                                                            not null,
    poidsMidiWeekend int                                                            not null,
    poidsSoirWeekend int                                                            not null,
    constraint PoidsMoment_Produits_id_fk
        foreign key (idProduit) references Produits (id)
);

-- Table Incompatibilite
create table Incompatibilite
(
    idProduit1 int not null,
    idProduit2 int not null,
    primary key (idProduit1, idProduit2),
    constraint Incompatibilite_ibfk_1
        foreign key (idProduit1) references Produits (id),
    constraint Incompatibilite_ibfk_2
        foreign key (idProduit2) references Produits (id)
);

create index idProduit2
    on Incompatibilite (idProduit2);

```
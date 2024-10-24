# vue de la base de données

## Vue Menu Semaine 

### Usage :
```sql 
SELECT * FROM VueMenuSemaine;
```

```sql
CREATE VIEW VueMenuSemaine AS
SELECT 
    Menu.menu_id,
    e1.nom_entree AS lundi_midi_entree,
    p1.nom_plat AS lundi_midi_plat,
    e2.nom_entree AS lundi_soir_entree,
    p2.nom_plat AS lundi_soir_plat,
    e3.nom_entree AS mardi_midi_entree,
    p3.nom_plat AS mardi_midi_plat,
    e4.nom_entree AS mardi_soir_entree,
    p4.nom_plat AS mardi_soir_plat
    -- Ajoute les autres jours ici
FROM Menu
JOIN Repas r1 ON Menu.lundi_midi_repas_id = r1.repas_id
JOIN Entree e1 ON r1.entree_id = e1.entree_id
JOIN Plat p1 ON r1.plat_id = p1.plat_id
JOIN Repas r2 ON Menu.lundi_soir_repas_id = r2.repas_id
JOIN Entree e2 ON r2.entree_id = e2.entree_id
JOIN Plat p2 ON r2.plat_id = p2.plat_id
JOIN Repas r3 ON Menu.mardi_midi_repas_id = r3.repas_id
JOIN Entree e3 ON r3.entree_id = e3.entree_id
JOIN Plat p3 ON r3.plat_id = p3.plat_id
JOIN Repas r4 ON Menu.mardi_soir_repas_id = r4.repas_id
JOIN Entree e4 ON r4.entree_id = e4.entree_id
JOIN Plat p4 ON r4.plat_id = p4.plat_id;
```
```sql
CREATE VIEW VuePoidsPlats AS
SELECT
    p.plat_id,
    p.nom_plat,
    pf.poids AS poids_famille,
    ps.poids AS poids_saison,
    pmj.poids AS poids_moment_journee,
    pms.poids AS poids_moment_semaine
FROM Plat p
         LEFT JOIN PoidsFamille pf ON p.plat_id = pf.plat_id
         LEFT JOIN PoidsSaison ps ON p.plat_id = ps.plat_id
         LEFT JOIN PoidsMomentJournee pmj ON p.plat_id = pmj.plat_id
         LEFT JOIN PoidsMomentSemaine pms ON p.plat_id = pms.plat_id;
```
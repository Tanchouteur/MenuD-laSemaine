# vue de la base de données

## Vue Menu Semaine 

### Usage :
```sql 
SELECT * FROM VueMenuSemaine;
```

```sql
CREATE OR REPLACE VIEW VueMenuSemaine AS
SELECT
    Menu.menu_id,
    e1.nom_entree AS lundi_midi_entree,
    CASE
        WHEN cc.nom_plat IS NOT NULL THEN cc.nom_plat
        ELSE CONCAT(
                v1.nom_viande,
                CASE
                    WHEN l1.nom_legume IS NOT NULL OR f1.nom_feculent IS NOT NULL THEN ' ('
                    ELSE ''
                    END,
                IFNULL(l1.nom_legume, ''),
                CASE
                    WHEN l1.nom_legume IS NOT NULL AND f1.nom_feculent IS NOT NULL THEN ' + '
                    ELSE ''
                    END,
                IFNULL(f1.nom_feculent, ''),
                CASE
                    WHEN l1.nom_legume IS NOT NULL OR f1.nom_feculent IS NOT NULL THEN ')'
                    ELSE ''
                    END
             )
        END AS lundi_midi_plat,
    e2.nom_entree AS lundi_soir_entree,
    CASE
        WHEN cc2.nom_plat IS NOT NULL THEN cc2.nom_plat
        ELSE CONCAT(
                v2.nom_viande,
                CASE
                    WHEN l2.nom_legume IS NOT NULL OR f2.nom_feculent IS NOT NULL THEN ' ('
                    ELSE ''
                    END,
                IFNULL(l2.nom_legume, ''),
                CASE
                    WHEN l2.nom_legume IS NOT NULL AND f2.nom_feculent IS NOT NULL THEN ' + '
                    ELSE ''
                    END,
                IFNULL(f2.nom_feculent, ''),
                CASE
                    WHEN l2.nom_legume IS NOT NULL OR f2.nom_feculent IS NOT NULL THEN ')'
                    ELSE ''
                    END
             )
        END AS lundi_soir_plat,
    e3.nom_entree AS mardi_midi_entree,
    CASE
        WHEN cc3.nom_plat IS NOT NULL THEN cc3.nom_plat
        ELSE CONCAT(
                v3.nom_viande,
                CASE
                    WHEN l3.nom_legume IS NOT NULL OR f3.nom_feculent IS NOT NULL THEN ' ('
                    ELSE ''
                    END,
                IFNULL(l3.nom_legume, ''),
                CASE
                    WHEN l3.nom_legume IS NOT NULL AND f3.nom_feculent IS NOT NULL THEN ' + '
                    ELSE ''
                    END,
                IFNULL(f3.nom_feculent, ''),
                CASE
                    WHEN l3.nom_legume IS NOT NULL OR f3.nom_feculent IS NOT NULL THEN ')'
                    ELSE ''
                    END
             )
        END AS mardi_midi_plat,
    e4.nom_entree AS mardi_soir_entree,
    CASE
        WHEN cc4.nom_plat IS NOT NULL THEN cc4.nom_plat
        ELSE CONCAT(
                v4.nom_viande,
                CASE
                    WHEN l4.nom_legume IS NOT NULL OR f4.nom_feculent IS NOT NULL THEN ' ('
                    ELSE ''
                    END,
                IFNULL(l4.nom_legume, ''),
                CASE
                    WHEN l4.nom_legume IS NOT NULL AND f4.nom_feculent IS NOT NULL THEN ' + '
                    ELSE ''
                    END,
                IFNULL(f4.nom_feculent, ''),
                CASE
                    WHEN l4.nom_legume IS NOT NULL OR f4.nom_feculent IS NOT NULL THEN ')'
                    ELSE ''
                    END
             )
        END AS mardi_soir_plat
-- Ajoutez d'autres jours ici si nécessaire
FROM Menu
         JOIN Repas r1 ON Menu.lundi_midi_repas_id = r1.repas_id
         JOIN Entree e1 ON r1.entree_id = e1.entree_id
         JOIN Plat p1 ON r1.plat_id = p1.plat_id
         LEFT JOIN PlatComplet cc ON p1.plat_id = cc.plat_id
         LEFT JOIN PlatCompose pc1 ON p1.plat_id = pc1.plat_id
         LEFT JOIN Viande v1 ON pc1.viande_id = v1.viande_id
         LEFT JOIN Accompagnement a1 ON pc1.accompagnement_id = a1.accompagnement_id
         LEFT JOIN Legume l1 ON a1.legume_id = l1.legume_id
         LEFT JOIN Feculent f1 ON a1.feculent_id = f1.feculent_id
         JOIN Repas r2 ON Menu.lundi_soir_repas_id = r2.repas_id
         JOIN Entree e2 ON r2.entree_id = e2.entree_id
         JOIN Plat p2 ON r2.plat_id = p2.plat_id
         LEFT JOIN PlatComplet cc2 ON p2.plat_id = cc2.plat_id
         LEFT JOIN PlatCompose pc2 ON p2.plat_id = pc2.plat_id
         LEFT JOIN Viande v2 ON pc2.viande_id = v2.viande_id
         LEFT JOIN Accompagnement a2 ON pc2.accompagnement_id = a2.accompagnement_id
         LEFT JOIN Legume l2 ON a2.legume_id = l2.legume_id
         LEFT JOIN Feculent f2 ON a2.feculent_id = f2.feculent_id
         JOIN Repas r3 ON Menu.mardi_midi_repas_id = r3.repas_id
         JOIN Entree e3 ON r3.entree_id = e3.entree_id
         JOIN Plat p3 ON r3.plat_id = p3.plat_id
         LEFT JOIN PlatComplet cc3 ON p3.plat_id = cc3.plat_id
         LEFT JOIN PlatCompose pc3 ON p3.plat_id = pc3.plat_id
         LEFT JOIN Viande v3 ON pc3.viande_id = v3.viande_id
         LEFT JOIN Accompagnement a3 ON pc3.accompagnement_id = a3.accompagnement_id
         LEFT JOIN Legume l3 ON a3.legume_id = l3.legume_id
         LEFT JOIN Feculent f3 ON a3.feculent_id = f3.feculent_id
         JOIN Repas r4 ON Menu.mardi_soir_repas_id = r4.repas_id
         JOIN Entree e4 ON r4.entree_id = e4.entree_id
         JOIN Plat p4 ON r4.plat_id = p4.plat_id
         LEFT JOIN PlatComplet cc4 ON p4.plat_id = cc4.plat_id
         LEFT JOIN PlatCompose pc4 ON p4.plat_id = pc4.plat_id
         LEFT JOIN Viande v4 ON pc4.viande_id = v4.viande_id
         LEFT JOIN Accompagnement a4 ON pc4.accompagnement_id = a4.accompagnement_id
         LEFT JOIN Legume l4 ON a4.legume_id = l4.legume_id
         LEFT JOIN Feculent f4 ON a4.feculent_id = f4.feculent_id;

```
```sql
CREATE OR REPLACE VIEW VuePoidsPlats AS
SELECT
    p.plat_id,
    CASE
        WHEN cc.nom_plat IS NOT NULL THEN cc.nom_plat
        ELSE CONCAT(
                v.nom_viande,
                CASE
                    WHEN l.nom_legume IS NOT NULL OR f.nom_feculent IS NOT NULL THEN ' ('
                    ELSE ''
                    END,
                IFNULL(l.nom_legume, ''),
                CASE
                    WHEN l.nom_legume IS NOT NULL AND f.nom_feculent IS NOT NULL THEN ' + '
                    ELSE ''
                    END,
                IFNULL(f.nom_feculent, ''),
                CASE
                    WHEN l.nom_legume IS NOT NULL OR f.nom_feculent IS NOT NULL THEN ')'
                    ELSE ''
                    END
             )
        END AS nom_plat,  -- Concatène le nom de la viande avec le légume et le féculent
    pf.poids AS poids_famille,
    ps.poids AS poids_saison,
    pmj.poids AS poids_moment_journee,
    pms.poids AS poids_moment_semaine
FROM Plat p
         LEFT JOIN PlatComplet cc ON p.plat_id = cc.plat_id  -- Jointure avec PlatComplet
         LEFT JOIN PlatCompose pc ON p.plat_id = pc.plat_id  -- Jointure avec PlatCompose pour obtenir les détails
         LEFT JOIN Viande v ON pc.viande_id = v.viande_id
         LEFT JOIN Accompagnement a ON pc.accompagnement_id = a.accompagnement_id
         LEFT JOIN Legume l ON a.legume_id = l.legume_id
         LEFT JOIN Feculent f ON a.feculent_id = f.feculent_id
         LEFT JOIN PoidsFamille pf ON p.plat_id = pf.plat_id
         LEFT JOIN PoidsSaison ps ON p.plat_id = ps.plat_id
         LEFT JOIN PoidsMomentJournee pmj ON p.plat_id = pmj.plat_id
         LEFT JOIN PoidsMomentSemaine pms ON p.plat_id = pms.plat_id;

```
-- Existing recipes and plans stay unchanged. Ingredients must be selected
-- explicitly before the generator may use them in composed meals.
ALTER TABLE "Ingredient"
  ADD COLUMN "useInComposedMeals" BOOLEAN NOT NULL DEFAULT false;

ALTER TABLE "AppSettings"
  ADD COLUMN "compositionSetupCompleted" BOOLEAN NOT NULL DEFAULT false;

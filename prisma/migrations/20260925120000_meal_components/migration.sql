CREATE TYPE "RecipeRole" AS ENUM ('MAIN', 'STARTER', 'SIDE_STARCH', 'SIDE_VEGETABLE');

ALTER TABLE "Ingredient" ADD COLUMN "useAsStarter" BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE "Recipe"
  ADD COLUMN "role" "RecipeRole" NOT NULL DEFAULT 'MAIN',
  ADD COLUMN "allowStarchSide" BOOLEAN NOT NULL DEFAULT false,
  ADD COLUMN "allowVegetableSide" BOOLEAN NOT NULL DEFAULT false,
  ADD COLUMN "variantOfId" TEXT;
ALTER TABLE "AppSettings" ADD COLUMN "starterTargetPerWeek" INTEGER NOT NULL DEFAULT 3;
ALTER TABLE "AppSettings" ADD CONSTRAINT "AppSettings_starterTarget_check"
  CHECK ("starterTargetPerWeek" BETWEEN 0 AND 9);

ALTER TABLE "MealSlot"
  ADD COLUMN "starterSnapshot" JSONB,
  ADD COLUMN "starterIsLocked" BOOLEAN NOT NULL DEFAULT false,
  ADD COLUMN "starterIngredientId" TEXT,
  ADD COLUMN "starterRecipeId" TEXT,
  ADD COLUMN "starchRecipeId" TEXT,
  ADD COLUMN "vegetableRecipeId" TEXT;

ALTER TABLE "Recipe" ADD CONSTRAINT "Recipe_variantOfId_fkey"
  FOREIGN KEY ("variantOfId") REFERENCES "Recipe"("id") ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE "MealSlot" ADD CONSTRAINT "MealSlot_starterIngredientId_fkey"
  FOREIGN KEY ("starterIngredientId") REFERENCES "Ingredient"("id") ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE "MealSlot" ADD CONSTRAINT "MealSlot_starterRecipeId_fkey"
  FOREIGN KEY ("starterRecipeId") REFERENCES "Recipe"("id") ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE "MealSlot" ADD CONSTRAINT "MealSlot_starchRecipeId_fkey"
  FOREIGN KEY ("starchRecipeId") REFERENCES "Recipe"("id") ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE "MealSlot" ADD CONSTRAINT "MealSlot_vegetableRecipeId_fkey"
  FOREIGN KEY ("vegetableRecipeId") REFERENCES "Recipe"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

CREATE INDEX "Recipe_role_isActive_idx" ON "Recipe"("role", "isActive");
CREATE INDEX "Recipe_variantOfId_idx" ON "Recipe"("variantOfId");

ALTER TABLE "MealSlot" DROP CONSTRAINT "MealSlot_content_check";
ALTER TABLE "MealSlot" ADD CONSTRAINT "MealSlot_content_check" CHECK (
  ("starchId" IS NULL OR "starchRecipeId" IS NULL) AND
  ("vegetableId" IS NULL OR "vegetableRecipeId" IS NULL) AND
  ("starterIngredientId" IS NULL OR "starterRecipeId" IS NULL) AND
  (
    ("slotType" = 'EMPTY' AND "recipeId" IS NULL AND "proteinId" IS NULL
      AND "starchId" IS NULL AND "vegetableId" IS NULL AND "starchRecipeId" IS NULL
      AND "vegetableRecipeId" IS NULL AND "leftoversFromSlotId" IS NULL
      AND "customLabel" IS NULL AND "mealSignature" IS NULL)
    OR
    ("slotType" = 'RECIPE' AND "recipeId" IS NOT NULL AND "proteinId" IS NULL
      AND "leftoversFromSlotId" IS NULL AND "mealSignature" IS NOT NULL)
    OR
    ("slotType" = 'COMPOSED' AND "recipeId" IS NULL AND "proteinId" IS NOT NULL
      AND ("starchId" IS NOT NULL OR "vegetableId" IS NOT NULL
        OR "starchRecipeId" IS NOT NULL OR "vegetableRecipeId" IS NOT NULL)
      AND "leftoversFromSlotId" IS NULL AND "mealSignature" IS NOT NULL)
    OR
    ("slotType" = 'LEFTOVERS' AND "recipeId" IS NULL AND "proteinId" IS NULL
      AND "starchId" IS NULL AND "vegetableId" IS NULL AND "starchRecipeId" IS NULL
      AND "vegetableRecipeId" IS NULL)
    OR
    ("slotType" = 'EATING_OUT' AND "recipeId" IS NULL AND "proteinId" IS NULL
      AND "starchId" IS NULL AND "vegetableId" IS NULL AND "starchRecipeId" IS NULL
      AND "vegetableRecipeId" IS NULL AND "leftoversFromSlotId" IS NULL)
    OR
    ("slotType" = 'CUSTOM' AND "customLabel" IS NOT NULL
      AND LENGTH(TRIM("customLabel")) > 0 AND "recipeId" IS NULL
      AND "proteinId" IS NULL AND "starchId" IS NULL AND "vegetableId" IS NULL
      AND "starchRecipeId" IS NULL AND "vegetableRecipeId" IS NULL
      AND "leftoversFromSlotId" IS NULL)
  )
);

-- CreateSchema
CREATE SCHEMA IF NOT EXISTS "public";

-- CreateEnum
CREATE TYPE "PlanStatus" AS ENUM ('DRAFT', 'CONFIRMED', 'ARCHIVED');

-- CreateEnum
CREATE TYPE "MealTime" AS ENUM ('LUNCH', 'DINNER');

-- CreateEnum
CREATE TYPE "SlotType" AS ENUM ('EMPTY', 'COMPOSED', 'RECIPE', 'LEFTOVERS', 'EATING_OUT', 'CUSTOM');

-- CreateEnum
CREATE TYPE "Season" AS ENUM ('WINTER', 'SPRING', 'SUMMER', 'AUTUMN');

-- CreateEnum
CREATE TYPE "IngredientCategory" AS ENUM ('PROTEIN', 'STARCH', 'VEGETABLE', 'GROCERY', 'DAIRY', 'OTHER');

-- CreateEnum
CREATE TYPE "QuantityUnit" AS ENUM ('GRAM', 'KILOGRAM', 'MILLILITER', 'CENTILITER', 'LITER', 'PIECE', 'SLICE', 'CAN');

-- CreateTable
CREATE TABLE "WeeklyPlan" (
    "id" TEXT NOT NULL,
    "startDate" DATE NOT NULL,
    "status" "PlanStatus" NOT NULL DEFAULT 'DRAFT',
    "isFavorite" BOOLEAN NOT NULL DEFAULT false,
    "duplicatedFromId" TEXT,
    "version" INTEGER NOT NULL DEFAULT 1,
    "confirmedAt" TIMESTAMP(3),
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "WeeklyPlan_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "MealSlot" (
    "id" TEXT NOT NULL,
    "weeklyPlanId" TEXT NOT NULL,
    "mealDate" DATE NOT NULL,
    "mealTime" "MealTime" NOT NULL,
    "guestCount" INTEGER NOT NULL DEFAULT 1,
    "isLocked" BOOLEAN NOT NULL DEFAULT false,
    "slotType" "SlotType" NOT NULL DEFAULT 'EMPTY',
    "customLabel" TEXT,
    "mealSignature" TEXT,
    "mealSnapshot" JSONB,
    "recipeId" TEXT,
    "proteinId" TEXT,
    "starchId" TEXT,
    "vegetableId" TEXT,
    "leftoversFromSlotId" TEXT,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "MealSlot_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "Ingredient" (
    "id" TEXT NOT NULL,
    "name" TEXT NOT NULL,
    "category" "IngredientCategory" NOT NULL,
    "subFamily" TEXT,
    "rating" INTEGER NOT NULL DEFAULT 3,
    "isActive" BOOLEAN NOT NULL DEFAULT true,
    "portionPerPerson" DECIMAL(10,3),
    "unit" "QuantityUnit",
    "aisleId" TEXT,
    "seasons" "Season"[] DEFAULT ARRAY['WINTER', 'SPRING', 'SUMMER', 'AUTUMN']::"Season"[],
    "okLunchWeekday" BOOLEAN NOT NULL DEFAULT true,
    "okDinnerWeekday" BOOLEAN NOT NULL DEFAULT true,
    "okLunchWeekend" BOOLEAN NOT NULL DEFAULT true,
    "okDinnerWeekend" BOOLEAN NOT NULL DEFAULT true,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "Ingredient_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "Recipe" (
    "id" TEXT NOT NULL,
    "name" TEXT NOT NULL,
    "style" TEXT,
    "rating" INTEGER NOT NULL DEFAULT 3,
    "prepTimeMinutes" INTEGER,
    "cookTimeMinutes" INTEGER,
    "basePortions" INTEGER NOT NULL DEFAULT 4,
    "isActive" BOOLEAN NOT NULL DEFAULT true,
    "seasons" "Season"[] DEFAULT ARRAY['WINTER', 'SPRING', 'SUMMER', 'AUTUMN']::"Season"[],
    "okLunchWeekday" BOOLEAN NOT NULL DEFAULT true,
    "okDinnerWeekday" BOOLEAN NOT NULL DEFAULT true,
    "okLunchWeekend" BOOLEAN NOT NULL DEFAULT true,
    "okDinnerWeekend" BOOLEAN NOT NULL DEFAULT true,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "Recipe_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "RecipeIngredient" (
    "id" TEXT NOT NULL,
    "recipeId" TEXT NOT NULL,
    "ingredientId" TEXT NOT NULL,
    "quantity" DECIMAL(10,3) NOT NULL,
    "unit" "QuantityUnit" NOT NULL,

    CONSTRAINT "RecipeIngredient_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "Aisle" (
    "id" TEXT NOT NULL,
    "name" TEXT NOT NULL,
    "sortOrder" INTEGER NOT NULL DEFAULT 0,

    CONSTRAINT "Aisle_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "Incompatibility" (
    "id" TEXT NOT NULL,
    "ingredientId1" TEXT NOT NULL,
    "ingredientId2" TEXT NOT NULL,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "Incompatibility_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "ShoppingListEntry" (
    "id" TEXT NOT NULL,
    "weeklyPlanId" TEXT NOT NULL,
    "stableKey" TEXT NOT NULL,
    "label" TEXT NOT NULL,
    "quantity" DECIMAL(12,3),
    "unit" "QuantityUnit",
    "aisleId" TEXT,
    "isChecked" BOOLEAN NOT NULL DEFAULT false,
    "isManual" BOOLEAN NOT NULL DEFAULT false,
    "sourceSlotIds" JSONB,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "ShoppingListEntry_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "AppSettings" (
    "id" TEXT NOT NULL DEFAULT 'default',
    "defaultGuestsLunchWeekday" INTEGER NOT NULL DEFAULT 2,
    "defaultGuestsDinnerWeekday" INTEGER NOT NULL DEFAULT 4,
    "defaultGuestsLunchWeekend" INTEGER NOT NULL DEFAULT 4,
    "defaultGuestsDinnerWeekend" INTEGER NOT NULL DEFAULT 4,
    "timezone" TEXT NOT NULL DEFAULT 'Europe/Paris',
    "onboardingCompleted" BOOLEAN NOT NULL DEFAULT false,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "AppSettings_pkey" PRIMARY KEY ("id")
);

-- Domain constraints intentionally maintained in SQL because Prisma cannot
-- express these cross-field checks in its schema language.
ALTER TABLE "WeeklyPlan"
  ADD CONSTRAINT "WeeklyPlan_startDate_monday_check"
    CHECK (EXTRACT(ISODOW FROM "startDate") = 1),
  ADD CONSTRAINT "WeeklyPlan_confirmation_check"
    CHECK ("status" = 'DRAFT' OR "confirmedAt" IS NOT NULL);

ALTER TABLE "MealSlot"
  ADD CONSTRAINT "MealSlot_guestCount_check" CHECK ("guestCount" > 0),
  ADD CONSTRAINT "MealSlot_content_check" CHECK (
    ("slotType" = 'EMPTY'
      AND "recipeId" IS NULL AND "proteinId" IS NULL AND "starchId" IS NULL
      AND "vegetableId" IS NULL AND "leftoversFromSlotId" IS NULL
      AND "customLabel" IS NULL AND "mealSignature" IS NULL)
    OR
    ("slotType" = 'RECIPE'
      AND "recipeId" IS NOT NULL AND "proteinId" IS NULL AND "starchId" IS NULL
      AND "vegetableId" IS NULL AND "leftoversFromSlotId" IS NULL
      AND "mealSignature" IS NOT NULL)
    OR
    ("slotType" = 'COMPOSED'
      AND "recipeId" IS NULL AND "proteinId" IS NOT NULL
      AND ("starchId" IS NOT NULL OR "vegetableId" IS NOT NULL)
      AND "leftoversFromSlotId" IS NULL AND "mealSignature" IS NOT NULL)
    OR
    ("slotType" = 'LEFTOVERS'
      AND "recipeId" IS NULL AND "proteinId" IS NULL AND "starchId" IS NULL
      AND "vegetableId" IS NULL)
    OR
    ("slotType" = 'EATING_OUT'
      AND "recipeId" IS NULL AND "proteinId" IS NULL AND "starchId" IS NULL
      AND "vegetableId" IS NULL AND "leftoversFromSlotId" IS NULL)
    OR
    ("slotType" = 'CUSTOM'
      AND "customLabel" IS NOT NULL AND LENGTH(TRIM("customLabel")) > 0
      AND "recipeId" IS NULL AND "proteinId" IS NULL AND "starchId" IS NULL
      AND "vegetableId" IS NULL AND "leftoversFromSlotId" IS NULL)
  );

ALTER TABLE "Ingredient"
  ALTER COLUMN "seasons" SET NOT NULL,
  ADD CONSTRAINT "Ingredient_rating_check" CHECK ("rating" BETWEEN 1 AND 5),
  ADD CONSTRAINT "Ingredient_portion_check"
    CHECK ("portionPerPerson" IS NULL OR "portionPerPerson" > 0),
  ADD CONSTRAINT "Ingredient_seasons_check" CHECK (CARDINALITY("seasons") > 0);

ALTER TABLE "Recipe"
  ALTER COLUMN "seasons" SET NOT NULL,
  ADD CONSTRAINT "Recipe_rating_check" CHECK ("rating" BETWEEN 1 AND 5),
  ADD CONSTRAINT "Recipe_basePortions_check" CHECK ("basePortions" > 0),
  ADD CONSTRAINT "Recipe_prepTime_check"
    CHECK ("prepTimeMinutes" IS NULL OR "prepTimeMinutes" >= 0),
  ADD CONSTRAINT "Recipe_cookTime_check"
    CHECK ("cookTimeMinutes" IS NULL OR "cookTimeMinutes" >= 0),
  ADD CONSTRAINT "Recipe_seasons_check" CHECK (CARDINALITY("seasons") > 0);

ALTER TABLE "RecipeIngredient"
  ADD CONSTRAINT "RecipeIngredient_quantity_check" CHECK ("quantity" > 0);

ALTER TABLE "Incompatibility"
  ADD CONSTRAINT "Incompatibility_distinct_check"
    CHECK ("ingredientId1" < "ingredientId2");

ALTER TABLE "ShoppingListEntry"
  ADD CONSTRAINT "ShoppingListEntry_quantity_check"
    CHECK ("quantity" IS NULL OR "quantity" > 0);

ALTER TABLE "AppSettings"
  ADD CONSTRAINT "AppSettings_guestCounts_check" CHECK (
    "defaultGuestsLunchWeekday" > 0
    AND "defaultGuestsDinnerWeekday" > 0
    AND "defaultGuestsLunchWeekend" > 0
    AND "defaultGuestsDinnerWeekend" > 0
  );

-- CreateIndex
CREATE UNIQUE INDEX "WeeklyPlan_startDate_key" ON "WeeklyPlan"("startDate");

-- CreateIndex
CREATE INDEX "WeeklyPlan_status_startDate_idx" ON "WeeklyPlan"("status", "startDate");

-- CreateIndex
CREATE INDEX "MealSlot_mealSignature_mealDate_idx" ON "MealSlot"("mealSignature", "mealDate");

-- CreateIndex
CREATE INDEX "MealSlot_recipeId_idx" ON "MealSlot"("recipeId");

-- CreateIndex
CREATE INDEX "MealSlot_proteinId_idx" ON "MealSlot"("proteinId");

-- CreateIndex
CREATE INDEX "MealSlot_starchId_idx" ON "MealSlot"("starchId");

-- CreateIndex
CREATE INDEX "MealSlot_vegetableId_idx" ON "MealSlot"("vegetableId");

-- CreateIndex
CREATE UNIQUE INDEX "MealSlot_weeklyPlanId_mealDate_mealTime_key" ON "MealSlot"("weeklyPlanId", "mealDate", "mealTime");

-- CreateIndex
CREATE UNIQUE INDEX "Ingredient_name_key" ON "Ingredient"("name");

-- CreateIndex
CREATE INDEX "Ingredient_category_isActive_idx" ON "Ingredient"("category", "isActive");

-- CreateIndex
CREATE INDEX "Ingredient_subFamily_idx" ON "Ingredient"("subFamily");

-- CreateIndex
CREATE UNIQUE INDEX "Recipe_name_key" ON "Recipe"("name");

-- CreateIndex
CREATE INDEX "Recipe_isActive_idx" ON "Recipe"("isActive");

-- CreateIndex
CREATE INDEX "Recipe_style_idx" ON "Recipe"("style");

-- CreateIndex
CREATE INDEX "RecipeIngredient_ingredientId_idx" ON "RecipeIngredient"("ingredientId");

-- CreateIndex
CREATE UNIQUE INDEX "RecipeIngredient_recipeId_ingredientId_key" ON "RecipeIngredient"("recipeId", "ingredientId");

-- CreateIndex
CREATE UNIQUE INDEX "Aisle_name_key" ON "Aisle"("name");

-- CreateIndex
CREATE INDEX "Incompatibility_ingredientId2_idx" ON "Incompatibility"("ingredientId2");

-- CreateIndex
CREATE UNIQUE INDEX "Incompatibility_ingredientId1_ingredientId2_key" ON "Incompatibility"("ingredientId1", "ingredientId2");

-- CreateIndex
CREATE INDEX "ShoppingListEntry_weeklyPlanId_isChecked_idx" ON "ShoppingListEntry"("weeklyPlanId", "isChecked");

-- CreateIndex
CREATE INDEX "ShoppingListEntry_aisleId_idx" ON "ShoppingListEntry"("aisleId");

-- CreateIndex
CREATE UNIQUE INDEX "ShoppingListEntry_weeklyPlanId_stableKey_key" ON "ShoppingListEntry"("weeklyPlanId", "stableKey");

-- AddForeignKey
ALTER TABLE "WeeklyPlan" ADD CONSTRAINT "WeeklyPlan_duplicatedFromId_fkey" FOREIGN KEY ("duplicatedFromId") REFERENCES "WeeklyPlan"("id") ON DELETE SET NULL ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "MealSlot" ADD CONSTRAINT "MealSlot_weeklyPlanId_fkey" FOREIGN KEY ("weeklyPlanId") REFERENCES "WeeklyPlan"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "MealSlot" ADD CONSTRAINT "MealSlot_recipeId_fkey" FOREIGN KEY ("recipeId") REFERENCES "Recipe"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "MealSlot" ADD CONSTRAINT "MealSlot_proteinId_fkey" FOREIGN KEY ("proteinId") REFERENCES "Ingredient"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "MealSlot" ADD CONSTRAINT "MealSlot_starchId_fkey" FOREIGN KEY ("starchId") REFERENCES "Ingredient"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "MealSlot" ADD CONSTRAINT "MealSlot_vegetableId_fkey" FOREIGN KEY ("vegetableId") REFERENCES "Ingredient"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "MealSlot" ADD CONSTRAINT "MealSlot_leftoversFromSlotId_fkey" FOREIGN KEY ("leftoversFromSlotId") REFERENCES "MealSlot"("id") ON DELETE SET NULL ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "Ingredient" ADD CONSTRAINT "Ingredient_aisleId_fkey" FOREIGN KEY ("aisleId") REFERENCES "Aisle"("id") ON DELETE SET NULL ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "RecipeIngredient" ADD CONSTRAINT "RecipeIngredient_recipeId_fkey" FOREIGN KEY ("recipeId") REFERENCES "Recipe"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "RecipeIngredient" ADD CONSTRAINT "RecipeIngredient_ingredientId_fkey" FOREIGN KEY ("ingredientId") REFERENCES "Ingredient"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "Incompatibility" ADD CONSTRAINT "Incompatibility_ingredientId1_fkey" FOREIGN KEY ("ingredientId1") REFERENCES "Ingredient"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "Incompatibility" ADD CONSTRAINT "Incompatibility_ingredientId2_fkey" FOREIGN KEY ("ingredientId2") REFERENCES "Ingredient"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "ShoppingListEntry" ADD CONSTRAINT "ShoppingListEntry_weeklyPlanId_fkey" FOREIGN KEY ("weeklyPlanId") REFERENCES "WeeklyPlan"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "ShoppingListEntry" ADD CONSTRAINT "ShoppingListEntry_aisleId_fkey" FOREIGN KEY ("aisleId") REFERENCES "Aisle"("id") ON DELETE SET NULL ON UPDATE CASCADE;

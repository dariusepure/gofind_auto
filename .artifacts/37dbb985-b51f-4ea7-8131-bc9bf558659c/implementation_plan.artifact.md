# Refine Car Discovery Logic

The goal is to streamline the "Find Your Next Car" screen by removing extra fields and making the remaining search criteria more flexible.

## Proposed Changes

### [Component: ViewModel]

#### [MODIFY] [AddCarViewModel.kt](file:///D:/Android%20Studio/GoFind%20Auto/app/src/main/java/com/dariusepure/autoadvisor/ui/cars/AddCarViewModel.kt)
- Remove `transmission`, `bodyType`, and `driveType` parameters from `getRecommendations`.
- Update the criteria builder to handle optional Brand/Model and partial ranges (e.g., just "from" or just "to").
- Example logic: If `yearFrom` is set but `yearTo` is blank, append "Produced after [yearFrom]".

### [Component: UI]

#### [MODIFY] [AddCarScreen.kt](file:///D:/Android%20Studio/GoFind%20Auto/app/src/main/java/com/dariusepure/autoadvisor/ui/cars/AddCarScreen.kt)
- Remove selection state and UI for Transmission, Body Type, and Drive Type.
- Update the **Generate** button:
  - Remove the strict requirement for Brand and Model (`enabled` should be `true` as long as *any* filter is applied, or simply always allowed if we want a "surprise me" AI mode).
- Update Brand/Model dropdowns:
  - Allow clearing the selection (add a "Clear" option or allow the field to be empty).
- Ensure `RangeInputRow` works correctly when only one field is filled.

## Verification Plan

### Manual Verification
- Test generating recommendations without selecting a Brand/Model.
- Test entering only "Year From" (e.g., 2020) and verify the AI suggests modern cars.
- Test entering only "Power To" (e.g., 100hp) and verify the AI suggests efficient/city cars.
- Verify that Transmission, Body Type, and Drive Type rows are gone.

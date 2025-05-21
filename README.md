# 💸 Application Mobile de Gestion Financière

Application Android complète développée avec **Android Studio** en **Java**, suivant l’architecture **MVVM**. Elle permet de suivre les revenus, les dépenses et les économies tout en affichant des statistiques claires. Les données sont stockées en temps réel via **Firebase Realtime Database**, et un **modèle de machine learning** intégré suggère automatiquement les catégories de dépenses à optimiser.

## 🔀 Branches

- `rollback-main` : branche principale contenant l'application mobile.
- `hiba-branche-ml` : branche dédiée au modèle de machine learning (fichiers Python + `.tflite`).

## 📱 Fonctionnalités Principales

### ✅ Authentification
- `MainActivity` : connexion avec `FirebaseAuth`
- `RegistrationActivity` : inscription utilisateur

### 💰 Revenu
- `IncomeFragment` : navigation entre :
  - `IncomeInsertFragment` : ajout de revenus
  - `IncomeListFragment` : affichage avec `RecyclerView` via `IncomeAdapter`
  - `IncomeChartFragment` : visualisation avec `PieChart` et `BarChart` via `MPAndroidChart`
- `IncomeViewModel` : gestion des revenus via `Firebase`

### 🧾 Dépenses
- `ExpenseActivity` : conteneur principal pour les fragments
- `ExpenseListFragment` : affichage des dépenses avec `ExpenseAdapter`
- `ExpenseChartFragment` : diagramme en camembert des catégories de dépenses
- `ExpenseViewModel` : gestion des dépenses sur `Firebase`

### 💹 Statistiques
- `ReportFragment` :
  - Graphiques dynamiques : `CombinedChart`, `LineChart`, `PieChart`
  - Vue d’ensemble des finances : revenus, dépenses, économies
  - Exportation des rapports en PDF

### 💼 Économies
- `SavingsFragment` :
  - Visualisation des économies en temps réel
  - `SavingsViewModel` : calcul automatique basé sur les données Firebase (revenus - dépenses)

## 🧠 Suggestion Automatique des Catégories à Réduire (ML)

Présente sur la branche `hiba-branche-ml`, cette fonctionnalité repose sur un modèle **TensorFlow/Keras** entraîné à partir du fichier `depenses.csv`.

### Étapes :
1. **Prétraitement** :
   - Nettoyage des colonnes
   - Encodage des étiquettes
   - Division des données en ensembles `train/test`

2. **Modélisation** :
   - Architecture : `Dense (16, relu)` → `Dense (8, relu)` → `Dense (softmax)`
   - Compilation avec `adam` et `categorical_crossentropy`

3. **Exportation** :
   - Sauvegarde au format **SavedModel**
   - Conversion en **TFLite** (`depense_model.tflite`)

## 🔧 Architecture MVVM

| **Composant**     | **Responsabilités**                                     |
|------------------|----------------------------------------------------------|
| `ViewModel`      | Interactions Firebase : récupération, ajout, suppression |
| `Fragments`      | Interface utilisateur : listes, formulaires, graphiques  |
| `Adapters`       | Affichage optimisé via `RecyclerView`                    |
| `Firebase`       | Base de données en temps réel                            |
| `ML Model`       | Recommandations sur les catégories à réduire             |



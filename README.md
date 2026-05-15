# Data Visualization and Communication Tool

## Overview
This Java-based desktop application is designed for importing, editing, and visualizing data from CSV files. It provides a comprehensive interface for data manipulation and multiple visualization techniques to help communicate data insights effectively.

## Key Features
- CSV Editor: Import CSV files, edit cell values, and manage rows or columns dynamically.
- Line and Bar Charts: Visualize trends and comparisons between different data series.
- Pie Charts: Display proportional data distribution.
- Heatmap: Visualize data density and intensity across numerical datasets.
- Custom Implementation: All visualizations are rendered using the Java Graphics2D API, ensuring efficient and library-independent chart generation.

## Technical Details
- Language: Java
- UI Framework: Java Swing
- Rendering Engine: Graphics2D
- Build System: Apache Maven

## Requirements
- Java Development Kit (JDK) 11 or higher
- Apache Maven

## How to Run
1. Navigate to the project root directory.
2. Build the project:
   ```bash
   mvn clean install
   ```
3. Execute the application:
   ```bash
   mvn exec:java -Dexec.mainClass="org.example.Main"
   ```

## Usage
- Load a CSV file using the CSV Editor tab.
- Switch to the visualization tabs (Line/Bar, Pie, Heatmap) to view the data.
- Charts update dynamically based on the changes made in the editor.

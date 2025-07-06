# db-sketch-benchmark

A Java-based benchmark to test the performance of various sketches.

## Requirements

- Java 24
- Maven 3.9.9 (or more recent)
- (optional) an IDE

## Build and run
The first thing you need to do after installing the requirements is to clone the repository.
```
git clone https://github.com/MarcDragt/db-sketch-benchmark
```
After this, navigate to the directory and use the following commands to build the project with Maven and run it. This can be done with the lines below. Alternatively, this can be done by opening the project using an IDE such as IntelliJ or NetBeans and running the Main.java file.
```
cd db-sketch-benchmark
mvn package assembly:single
java -jar target/BEP-1.0-jar-with-dependencies.jar
```
When any code has been changed, the following line can be used to compile the files with Maven.
```
mvn clean compile assembly:single
```
The benchmark results are now output to the console. The settings can be changed in the `Main.java` file.
The configuration of the run is determined by the variables `dataSize`, `querySize`, `dataset`, `numberOfDistincts`, `parameters`, and `sketchFactory`. The `dataSize` and `querySize` variables are integers determining the size of the inserted dataset and the size of the queried dataset. The String variable `dataset` determines what type of dataset is used in the benchmark, with the `numberOfDistincts` potentially limiting the number of unique elements. The `Object[][] parameters` takes the list of parameter settings that will be passed to the `sketchFactory` to create the sketches. All of these variables are required to run the benchmark, and can be set in the `Main.java` file, or any other file that calls the `Benchmark` class.

## Using the CAIDA dataset

If you are eligible to use the CAIDA Anonymised Internet Traces, access to the dataset can be requested at [this link](https://www.caida.org/catalog/datasets/request_user_info_forms/passive_dataset_request/). The data used for the included experiments can then be found at [this link](https://data.caida.org/datasets/passive-2009/equinix-chicago/20090115-130000.UTC/).

These files can be parsed with the included bash script. The CSV files should then be placed in the resources folder CAIDA.

## Adding a new sketch

Adding a new sketch can be done by simply adding the sketch to the `synopses` folder. This sketch should implement one of the included sketch types, or a sketch type that is also introduced. To run the benchmark on the sketch, a list of parameters to test and a factory for the sketch must be created. This factory can be implemented in the `Main.java` file and should provide the type of each argument the sketch accepts during instantiation. The factory of the Count-Min sketch looks like this.
```
SketchFactory<FrequencySynopsis> cmFactory = (arguments) -> new CM((int) arguments[0], (int) arguments[1]);
```
The parameters can then be an `Object[][]` corresponding to the arguments.

## Adding a new sketch type
Adding a new sketch type can be done by adding the sketch type to the `structures` folder. This sketch type should extend the `Synopsys` interface. Because this type will have a different error, the `Benchmark.java` file will need a new framework for this sketch type and a new method that measures the error. This framework can be adapted from other frameworks to output errors correctly. To run the benchmark, a new run method has to be created to feed the new sketch type into the framework. This method can also look largely the same as the other sketch types.

## Adding a new dataset
Adding a new dataset requires creating a new folder in the `resources` folder. This dataset then needs to be parsed, and a variable of interest must be extracted. This variable can also be a combination of different variables in the dataset. The method for parsing the variables should then be added to the `getData` method in `Benchmark` class so that it can be called in the benchmark.

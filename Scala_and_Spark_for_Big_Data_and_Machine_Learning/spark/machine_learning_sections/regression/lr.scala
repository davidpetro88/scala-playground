import org.apache.spark.ml.evaluation.RegressionEvaluator
import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.ml.tuning.{ParamGridBuilder, TrainValidationSplit}

// To see less warnings
import org.apache.log4j._
Logger.getLogger("org").setLevel(Level.ERROR)

// Start a simple Spark Session
import org.apache.spark.sql.SparkSession
val spark = SparkSession.builder().getOrCreate()
import org.apache.spark.sql.types._
val data1 = spark.read.option("header","true").option("inferSchema","true").format("csv").load("USA_Housing.csv")
val data2 = data1.select(
  data1.columns.map{
    case "Avg Area Income" => data1("Avg Area Income").cast(DoubleType).as("Avg Area Income")
    case "Avg Area House Age" => data1("Avg Area House Age").cast(DoubleType).as("Avg Area House Age")
    case "Avg Area Number of Rooms" => data1("Avg Area Number of Rooms").cast(DoubleType).as("Avg Area Number of Rooms")
    case "Avg Area Number of Bedrooms" => data1("Avg Area Number of Bedrooms").cast(DoubleType).as("Avg Area Number of Bedrooms")
    case "Area Population" => data1("Area Population").cast(DoubleType).as("Area Population")
    case "Price" => data1("Price").cast(DoubleType).as("Price")
    case "Address" => data1("Address").cast(StringType).as("Address")
  } : _*
)
// Check out the Data
data2.printSchema()
val  data = data2.na.drop()
// // Check out the Data
data.printSchema()

val colnames = data.columns
val firstrow = data.head(1)(0)
println("\n")
println("Example Data Row")
for(ind <- Range(1,colnames.length)){
  println(colnames(ind))
  println(firstrow(ind))
  println("\n")
}


//
// ////////////////////////////////////////////////////
// //// Setting Up DataFrame for Machine Learning ////
// //////////////////////////////////////////////////
//
// // A few things we need to do before Spark can accept the data!
// // It needs to be in the form of two columns
// // ("label","features")
//
// // This will allow us to join multiple feature columns
// // into a single column of an array of feautre values
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.ml.linalg.Vectors
// // Rename Price to label column for naming convention.
// // Grab only numerical columns from the data
val df = data.select(data("Price").as("label"),$"Avg Area Income",$"Avg Area House Age",$"Avg Area Number of Rooms",$"Area Population")
// //
// // // An assembler converts the input values to a vector
// // // A vector is what the ML algorithm reads to train a model
// //
// // // Set the input columns from which we are supposed to read the values
// // // Set the name of the column where the vector will be stored
val assembler = new VectorAssembler().setInputCols(Array("Avg Area Income","Avg Area House Age","Avg Area Number of Rooms","Area Population")).setOutputCol("features")
// // // Use the assembler to transform our DataFrame to the two columns
val output = assembler.transform(df).select($"label",$"features")
val lr = new LinearRegression()
// Fit the model to the data
// Note: Later we will see why we should split
// the data first, but for now we will fit to all the data.
val lrModel = lr.fit(output)
// Print the coefficients and intercept for linear regression
println(s"Coefficients: ${lrModel.coefficients} Intercept: ${lrModel.intercept}")
// Summarize the model over the training set and print out some metrics!
// Explore this in the spark-shell for more methods to call
val trainingSummary = lrModel.summary
println(s"numIterations: ${trainingSummary.totalIterations}")
println(s"objectiveHistory: ${trainingSummary.objectiveHistory.toList}")
//Residuals (label - predicted value)
trainingSummary.residuals.show()
//Returns the root mean squared error, which is defined as the square root of the mean squared error.
println(s"RMSE: ${trainingSummary.rootMeanSquaredError}")
//Returns the mean squared error, which is a risk function corresponding to the expected value of the squared error loss or quadratic loss.
println(s"MSE: ${trainingSummary.meanSquaredError}")
//Returns R^2^, the coefficient of determination.
println(s"r2: ${trainingSummary.r2}")
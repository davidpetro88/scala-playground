/**
  * Created by david on 12/01/17.
  */
import org.apache.spark.ml.evaluation.RegressionEvaluator
import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.ml.tuning.{ParamGridBuilder,TrainValidationSplit}

import org.apache.log4j._
Logger.getLogger("org").setLevel(Level.ERROR)

import org.apache.spark.sql.SparkSession
val spark = SparkSession.builder().getOrCreate()

val data1 = spark.read.option("header","true").option("InferSchema","true").format("csv").load("../regression/USA_Housing.csv")

// Convert Types
import org.apache.spark.sql.types._
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
val  data = data2.na.drop()

import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.ml.linalg.Vectors

val df = data.select(data("Price")as("label"),$"Avg Area Income", $"Avg Area House Age", $"Avg Area Number of Rooms",
$"Avg Area Number of Bedrooms", $"Area Population")

df.printSchema

val assembler = new VectorAssembler().setInputCols(Array("Avg Area Income", "Avg Area House Age",
"Avg Area Number of Rooms", "Avg Area Number of Bedrooms", "Area Population")).setOutputCol("features")

val output = assembler.transform(df).select($"label",$"features")

// TRAINING AND TEST data
val Array(training,test) = output.select("label","features").randomSplit(Array(0.7,0.3), seed=12345)

// model
val lr = new LinearRegression()

//Parameter Grid builder
val paramGrid = new ParamGridBuilder().addGrid(lr.regParam,Array(10000,01)).build()

// Train SPLIT (Holdout)
val trainvalsplit = (new TrainValidationSplit()
.setEstimator(lr)
.setEvaluator(new RegressionEvaluator().setMetricName("r2"))
.setEstimatorParamMaps(paramGrid)
.setTrainRatio(0.8))

val model = trainvalsplit.fit(training)

model.transform(test).select("features","label","prediction").show()

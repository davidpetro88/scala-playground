/**
  * Created by david on 10/01/17.
  */

//Logistic Regression Example
import com.sun.xml.internal.ws.api.pipe.PipelineAssembler
import org.apache.spark.ml.classification.LogisticRegression
import org.apache.spark.sql.SparkSession


// Optional: Use the following code below to set the Error reporting
import org.apache.log4j._
Logger.getLogger("org").setLevel(Level.ERROR)

// Create a Spark Session
val spark = SparkSession.builder().getOrCreate()

// Use Spark to read in the Advertising csv file.
val data = spark.read.option("header","true").option("inferSchema","true").format("csv").load("titanic.csv")

// Print the Schema of the DataFrame
data.printSchema()

///////////////////////
/// Display Data /////
/////////////////////

// Print out a sample row of the data (multiple ways to do this)
val colnames = data.columns
val firstrow = data.head(1)(0)
println("\n")
println("Example Data Row")
for(ind <- Range(1,colnames.length)){
  println(colnames(ind))
  println(firstrow(ind))
  println("\n")
}

val logregdataal = (data.select(data("Survived").as("label"), $"Pclass", $"Name", $"Sex", $"Age",
                    $"SibSp", $"Parch", $"Fare", $"Embarked"))

//drop null values
val logregdata = logregdataal.na.drop()

import org.apache.spark.ml.feature.{VectorAssembler,StringIndexer,VectorIndexer,OneHotEncoder}
import org.apache.spark.ml.linalg.Vectors

// Converting String into Numerical values
val genderIndexer = new StringIndexer().setInputCol("Sex").setOutputCol("SexIndex")
val embarkIndexer = new StringIndexer().setInputCol("Embarked").setOutputCol("EmbarkIndex")

//Converting Numerical Values into One Hot Encoding 0 or 1
val genderEncoder = new OneHotEncoder().setInputCol("SexIndex").setOutputCol("SexVec")
val embarkEncoder = new OneHotEncoder().setInputCol("EmbarkIndex").setOutputCol("EmbarkVec")

//(label,features)
val assembler = (new VectorAssembler().setInputCols(Array("Pclass","SexVec","Age",
                  "SibSp","Parch","Fare","EmbarkVec")).setOutputCol("features"))

val Array(trainning,test) = logregdata.randomSplit(Array(0.7,0.3),seed=12345)

import org.apache.spark.ml.Pipeline

val lr = new LogisticRegression()

val pipeline = new Pipeline().setStages(Array(genderIndexer, embarkIndexer, genderEncoder,embarkEncoder, assembler, lr))

// the Model only see to the label and features
val model = pipeline.fit(trainning)

val results = model.transform(test)

//
////////////////////////////////
// MODEL EVALUATION
////////////////////////////////
import org.apache.spark.mllib.evaluation.MulticlassMetrics

val predictionAndLabels = results.select($"prediction",$"label").as[(Double,Double)].rdd

//results.printSchema

val metrics = new MulticlassMetrics(predictionAndLabels)

println("Confusion Matrix:")
println(metrics.confusionMatrix)
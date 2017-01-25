//Start a simple Spark Session
import org.apache.spark.sql.SparkSession
val spark = SparkSession.builder().getOrCreate()

//Create a DataFrame from Spark Session read csv
//Technically known as class Dataset
val df = spark.read.option("header","true").option("inferSchema","true").csv("ContainsNull.csv")
df.printSchema
df.show()
//Remove Columns with null values
df.na.drop().show()
//Remove Columns with have 2 null values
df.na.drop(2).show()

//Fill with value 100. When the value is null (It see all datatype if match with the type I pass in the fill )
df.na.fill(100).show()
df.na.fill("Cevoscleu").show()

df.na.fill("New Name", Array("Name")).show()
df.na.fill(200, Array("Sales")).show()

df.describe().show()

val df2 =df.na.fill("New Name", Array("Name"))
df2.na.fill(400.5, Array("Sales")).show()

import org.apache.spark.sql.SparkSession

val spark = SparkSession.builder().getOrCreate()
//val df = spark.read.csv("CitiGroup2006_2008")
//df.head(5)

//separate the header and infer the type of values like Date -> Timestamp / Open -> Double
val df = spark.read.option("header","true").option("inferSchema","true").csv("CitiGroup2006_2008")
println("Loop All Values")
for (row <- df.head(5)) {
    println(row)
}

println("Describe")
df.describe().show()

println("Select Volume")
df.select("Volume").show()

println("Other select")
df.select($"Date", $"Close").show()

println("Add New Column df2")
val df2 = df.withColumn("HighPlusLow",df("High")+df("Low"))
// For retrun more put the name df2 or $ notation.
df2.select(df2("HighPlusLow").as("HPL"), df2("Close"), $"Date").show()

println("Print Schema df")
df.printSchema

import spark.implicits._
println("Filters ")
df.filter($"Close" > 480).show()
// or
df.filter("Close > 480").show()

df.filter($"Close" > 480 && $"High" > 480).show()
df.filter("Close > 480 AND High > 480").show()

val CH_low = df.filter("Close > 480 AND High > 480").collect()
val CH_low_count = df.filter("Close > 480 AND High > 480").count

df.filter($"High"===484.40).show()
df.filter("High = 484.40").show()

//Correlation
df.select(corr("High","Low")).show()

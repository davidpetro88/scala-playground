for(item <- List(1,2,3)){
  println("Hello item :" + item)
}

for(item <-Array.range(0,5)){
  println(item)
}

for(item <- Set(1,2,3)){
  println( item)
}

for(num <- Array.range(0,10)){
  if(num%2 == 0){
    println(s"$num is even")
  } else {
    println(s"$num is odd")
  }
}


val names = List("John","Abe","Cindy","Cat")
for(name <- names){
  if(name.startsWith("C")){
    println(s"$name starts with C")
  }
}

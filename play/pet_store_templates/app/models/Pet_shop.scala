package models

import java.util.concurrent.atomic.AtomicLong
import scala.collection.concurrent.TrieMap
import scala.concurrent.Future


case class Pet_Item(id: Long, name: String, price: Double)

trait Pet_Franchise {
  def list(): Seq[Pet_Item]
  def create(name: String, price: Double): Option[Pet_Item]
  def details(id: Long): Option[Pet_Item]
  def update(id: Long, name: String, price: Double): Option[Pet_Item]
  def delete(id: Long): Boolean
}

object Pet_Shop extends Pet_Franchise {

  /**
    * temp data store
    */
  private val items = TrieMap.empty[Long, Pet_Item]

  items.put(1, Pet_Item(1, "small rabbit", 12) )
  items.put(2, Pet_Item(2, "medium rabbit", 17) )
  items.put(3, Pet_Item(3, "chunky rabbit treats", 2) )


  private val seq = new AtomicLong

  def list(): Seq[Pet_Item] = items.values.to[Seq]

  def update(id: Long, name: String, price: Double): Option[Pet_Item]
  = {
    val item = Pet_Item(id, name, price)
    items.replace(id, item)
    Some(item)
  }

  def details(id: Long): Option[Pet_Item] = items.get(id)



  def delete(id: Long): Boolean = items.remove(id).isDefined

  def create(name: String, price: Double): Option[Pet_Item] = {
    val id = seq.incrementAndGet()
    val item = Pet_Item(id, name, price)
    items.put(id, item)
    Some(item)
  }
}


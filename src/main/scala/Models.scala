import slick.driver.H2Driver.api._
import com.bayakala.funda._
object Models {

  //表字段对应模版
  case class AQMRawModel(mid: String
                         , mtype: String
                         , state: String
                         , fips: String
                         , county: String
                         , year: String
                         , value: String
                         )

  //表结构: 定义字段类型, * 代表结果集字段
  class AQMRawTable(tag: Tag) extends Table[AQMRawModel](tag, "AIRQM") {
    def mid = column[String]("MEASUREID")
    def mtype = column[String]("MEASURETYPE")
    def state = column[String]("STATENAME")
    def fips = column[String]("COUNTYFIPS")
    def county = column[String]("COUNTYNAME")
    def year = column[String]("REPORTYEAR")
    def value = column[String]("VALUE")


    def * = (mid,mtype,state,fips,county,year,value) <> (AQMRawModel.tupled, AQMRawModel.unapply)
  }

  //库表实例
  val AQMRawQuery = TableQuery[AQMRawTable]

  case class AQMRPTModel(rid: Long
                         , mid: Int
                         , state: String
                         , county: String
                         , year: Int
                         , value: Int
                         , total: Int
                         , valid: Boolean) extends FDAROW

  class AQMRPTTable(tag: Tag) extends Table[AQMRPTModel](tag, "AQMRPT") {
    def rid = column[Long]("ROWID",O.AutoInc,O.PrimaryKey)
    def mid = column[Int]("MEASUREID")
    def state = column[String]("STATENAME",O.Length(32))
    def county = column[String]("COUNTYNAME",O.Length(32))
    def year = column[Int]("REPORTYEAR")
    def value = column[Int]("VALUE")
    def total = column[Int]("TOTAL")
    def valid = column[Boolean]("VALID")

    def * = (rid,mid,state,county,year,value,total,valid) <> (AQMRPTModel.tupled, AQMRPTModel.unapply)
  }


  val AQMRPTQuery = TableQuery[AQMRPTTable]


  class AlbumsTable(tag: Tag) extends Table[
    (Long,String,String,Option[Int],Int)](tag,"ALBUMS") {
    def id = column[Long]("ID",O.PrimaryKey)
    def title = column[String]("TITLE")
    def artist = column[String]("ARTIST")
    def year = column[Option[Int]]("YEAR")
    def company = column[Int]("COMPANY")
    def * = (id,title,artist,year,company)
  }
  val albums = TableQuery[AlbumsTable]
  class CompanyTable(tag: Tag) extends Table[(Int,String)](tag,"COMPANY") {
    def id = column[Int]("ID",O.PrimaryKey)
    def name = column[String]("NAME")
    def * = (id, name)
  }
  val companies = TableQuery[CompanyTable]

  val albumInfo =
    for {
      a <- albums
      c <- companies
      if (a.company === c.id)
    } yield(a.title,a.artist,a.year,c.name)



}
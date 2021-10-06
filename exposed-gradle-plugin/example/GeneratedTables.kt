import java.math.BigDecimal
import java.time.LocalDateTime
import kotlin.Int
import kotlin.String
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.datetime

object Artists : IntIdTable("artists", "artistid") {
  val name: Column<String?> = varchar("name", 120).nullable()
}

object Employees : IntIdTable("employees", "employeeid") {
  val lastName: Column<String> = varchar("lastname", 20)

  val firstName: Column<String> = varchar("firstname", 20)

  val title: Column<String?> = varchar("title", 30).nullable()

  val reportsTo: Column<Int?> =
      integer("reportsto").references(employeeId).index("ifk_employeereportsto").nullable()

  val birthDate: Column<LocalDateTime?> = datetime("birthdate").nullable()

  val hireDate: Column<LocalDateTime?> = datetime("hiredate").nullable()

  val address: Column<String?> = varchar("address", 70).nullable()

  val city: Column<String?> = varchar("city", 40).nullable()

  val state: Column<String?> = varchar("state", 40).nullable()

  val country: Column<String?> = varchar("country", 40).nullable()

  val postalCode: Column<String?> = varchar("postalcode", 10).nullable()

  val phone: Column<String?> = varchar("phone", 24).nullable()

  val fax: Column<String?> = varchar("fax", 24).nullable()

  val email: Column<String?> = varchar("email", 60).nullable()
}

object Genres : IntIdTable("genres", "genreid") {
  val name: Column<String?> = varchar("name", 120).nullable()
}

object MediaTypes : IntIdTable("media_types", "mediatypeid") {
  val name: Column<String?> = varchar("name", 120).nullable()
}

object Playlists : IntIdTable("playlists", "playlistid") {
  val name: Column<String?> = varchar("name", 120).nullable()
}

object Albums : IntIdTable("albums", "albumid") {
  val title: Column<String> = varchar("title", 160)

  val artistId: Column<Int> =
      integer("artistid").references(Artists.artistId).index("ifk_albumartistid")

  val `value`: Column<Int?> = integer("value").nullable()
}

object Customers : IntIdTable("customers", "customerid") {
  val firstName: Column<String> = varchar("firstname", 40)

  val lastName: Column<String> = varchar("lastname", 20)

  val company: Column<String?> = varchar("company", 80).nullable()

  val address: Column<String?> = varchar("address", 70).nullable()

  val city: Column<String?> = varchar("city", 40).nullable()

  val state: Column<String?> = varchar("state", 40).nullable()

  val country: Column<String?> = varchar("country", 40).nullable()

  val postalCode: Column<String?> = varchar("postalcode", 10).nullable()

  val phone: Column<String?> = varchar("phone", 24).nullable()

  val fax: Column<String?> = varchar("fax", 24).nullable()

  val email: Column<String> = varchar("email", 60)

  val supportRepId: Column<Int?> =
      integer("supportrepid").references(Employees.employeeId).index("ifk_customersupportrepid").nullable()
}

object Invoices : IntIdTable("invoices", "invoiceid") {
  val customerId: Column<Int> =
      integer("customerid").references(Customers.customerId).index("ifk_invoicecustomerid")

  val invoiceDate: Column<LocalDateTime> = datetime("invoicedate")

  val billingAddress: Column<String?> = varchar("billingaddress", 70).nullable()

  val billingCity: Column<String?> = varchar("billingcity", 40).nullable()

  val billingState: Column<String?> = varchar("billingstate", 40).nullable()

  val billingCountry: Column<String?> = varchar("billingcountry", 40).nullable()

  val billingPostalCode: Column<String?> = varchar("billingpostalcode", 10).nullable()

  val total: Column<BigDecimal> = decimal("total", 10, 2)
}

object Tracks : IntIdTable("tracks", "trackid") {
  val name: Column<String> = varchar("name", 200)

  val albumId: Column<Int?> =
      integer("albumid").references(Albums.albumId).index("ifk_trackalbumid").nullable()

  val mediaTypeId: Column<Int> =
      integer("mediatypeid").references(MediaTypes.mediaTypeId).index("ifk_trackmediatypeid")

  val genreId: Column<Int?> =
      integer("genreid").references(Genres.genreId).index("ifk_trackgenreid").nullable()

  val composer: Column<String?> = varchar("composer", 220).nullable()

  val milliseconds: Column<Int> = integer("milliseconds")

  val bytes: Column<Int?> = integer("bytes").nullable()

  val unitPrice: Column<BigDecimal> = decimal("unitprice", 10, 2)
}

object InvoiceItems : IntIdTable("invoice_items", "invoicelineid") {
  val invoiceId: Column<Int> =
      integer("invoiceid").references(Invoices.invoiceId).index("ifk_invoicelineinvoiceid")

  val trackId: Column<Int> =
      integer("trackid").references(Tracks.trackId).index("ifk_invoicelinetrackid")

  val unitPrice: Column<BigDecimal> = decimal("unitprice", 10, 2)

  val quantity: Column<Int> = integer("quantity")
}

object PlaylistTrack : Table("playlist_track") {
  val playlistId: Column<Int> = integer("playlistid").references(Playlists.playlistId)

  val trackId: Column<Int> =
      integer("trackid").references(Tracks.trackId).uniqueIndex("ifk_playlisttracktrackid")

  override val primaryKey: PrimaryKey = PrimaryKey(playlistId, trackId)

  init {
  }
}

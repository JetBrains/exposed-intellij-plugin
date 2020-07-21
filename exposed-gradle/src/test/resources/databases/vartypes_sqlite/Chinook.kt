package databases.vartypes_sqlite

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.datetime
import java.math.BigDecimal
import java.time.LocalDateTime

object Playlist : IntIdTable("playlist", "playlistid") {
    val name: Column<String?> = varchar("name", 120).nullable()
}

object MediaType : IntIdTable("mediatype", "mediatypeid") {
    val name: Column<String?> = varchar("name", 120).nullable()
}

object Genre : IntIdTable("genre", "genreid") {
    val name: Column<String?> = varchar("name", 120).nullable()
}

object Employee : IntIdTable("employee", "employeeid") {
    val lastName: Column<String> = varchar("lastname", 20)
    val firstName: Column<String> = varchar("firstname", 20)
    val title: Column<String?> = varchar("title", 30).nullable()
    val reportsTo: Column<Int?> = integer("reportsto").references(id).nullable()
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

object Customer : IntIdTable("customer", "customerid") {
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
    val supportRepId: Column<Int?> = integer("supportrepid").references(Employee.id).nullable()
}

object Invoice : IntIdTable("invoice", "invoiceid") {
    val customerId: Column<Int> = integer("customerid").references(Customer.id)
    val invoiceDate: Column<LocalDateTime> = datetime("invoicedate")
    val billingAddress: Column<String?> = varchar("billingaddress", 70).nullable()
    val billingCity: Column<String?> = varchar("billingcity", 40).nullable()
    val billingState: Column<String?> = varchar("billingstate", 40).nullable()
    val billingCountry: Column<String?> = varchar("billingcountry", 40).nullable()
    val billingPostalCode: Column<String?> = varchar("billingpostalcode", 10).nullable()
    val total: Column<BigDecimal> = decimal("total", 10, 2)
}

object Artist : IntIdTable("artist", "artistid") {
    val name: Column<String?> = varchar("name", 120).nullable()
}

object Album : IntIdTable("album", "albumid") {
    val title: Column<String> = varchar("title", 160)
    val artistId: Column<Int> = integer("artistid").references(Artist.id)
}

object Track : IntIdTable("track", "trackid") {
    val name: Column<String> = varchar("name", 200)
    val albumId: Column<Int?> = integer("albumid").references(Album.id).nullable()
    val mediaTypeId: Column<Int> = integer("mediatypeid").references(MediaType.id)
    val genreId: Column<Int?> = integer("genreid").references(Genre.id).nullable()
    val composer: Column<String?> = varchar("composer", 220).nullable()
    val milliseconds: Column<Int> = integer("milliseconds")
    val bytes: Column<Int?> = integer("bytes").nullable()
    val unitPrice: Column<BigDecimal> = decimal("unitprice", 10, 2)
}

object InvoiceLine : IntIdTable("invoiceline", "invoicelineid") {
    val invoiceId: Column<Int> = integer("invoiceid").references(Invoice.id)
    val trackId: Column<Int> = integer("trackid").references(Track.id)
    val unitPrice: Column<BigDecimal> = decimal("unitPrice", 10, 2)
    val quantity: Column<Int> = integer("quantity")
}

object PlaylistTrack : Table("playlisttrack") {
    val playlistId: Column<Int> = integer("playlistid").references(Playlist.id)
    val trackId: Column<Int> = integer("trackid").references(Track.id)
}

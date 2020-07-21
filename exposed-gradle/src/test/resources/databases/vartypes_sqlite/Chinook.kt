package databases.vartypes_sqlite

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.datetime
import java.math.BigDecimal
import java.time.LocalDateTime

object Playlist : IntIdTable("playlist", "playlistid") {
    val name: Column<String> = varchar("name", 120)
}

object MediaType : IntIdTable("mediatype", "mediatypeid") {
    val name: Column<String> = varchar("name", 120)
}

object Genre : IntIdTable("genre", "genreid") {
    val name: Column<String> = varchar("name", 120)
}

object Employee : IntIdTable("employee", "employeeid") {
    val lastName: Column<String> = varchar("lastname", 20)
    val firstName: Column<String> = varchar("firstname", 20)
    val title: Column<String> = varchar("title", 30)
    val reportsTo: Column<Int> = integer("reportsto").references(id)
    val birthDate: Column<LocalDateTime> = datetime("birthdate")
    val hireDate: Column<LocalDateTime> = datetime("hiredate")
    val address: Column<String> = varchar("address", 70)
    val city: Column<String> = varchar("city", 40)
    val state: Column<String> = varchar("state", 40)
    val country: Column<String> = varchar("country", 40)
    val postalCode: Column<String> = varchar("postalcode", 10)
    val phone: Column<String> = varchar("phone", 24)
    val fax: Column<String> = varchar("fax", 24)
    val email: Column<String> = varchar("email", 60)
}

object Customer : IntIdTable("customer", "customerid") {
    val firstName: Column<String> = varchar("firstname", 40)
    val lastName: Column<String> = varchar("lastname", 20)
    val company: Column<String> = varchar("company", 80)
    val address: Column<String> = varchar("address", 70)
    val city: Column<String> = varchar("city", 40)
    val state: Column<String> = varchar("state", 40)
    val country: Column<String> = varchar("country", 40)
    val postalCode: Column<String> = varchar("postalcode", 10)
    val phone: Column<String> = varchar("phone", 24)
    val fax: Column<String> = varchar("fax", 24)
    val email: Column<String> = varchar("email", 60)
    val supportRepId: Column<Int> = integer("supportrepid").references(Employee.id)
}

object Invoice : IntIdTable("invoice", "invoiceid") {
    val customerId: Column<Int> = integer("customerid").references(Customer.id)
    val invoiceDate: Column<LocalDateTime> = datetime("invoicedate")
    val billingAddress: Column<String> = varchar("billingaddress", 70)
    val billingCity: Column<String> = varchar("billingcity", 40)
    val billingState: Column<String> = varchar("billingstate", 40)
    val billingCountry: Column<String> = varchar("billingcountry", 40)
    val billingPostalCode: Column<String> = varchar("billingpostalcode", 10)
    val total: Column<BigDecimal> = decimal("total", 10, 2)
}

object Artist : IntIdTable("artist", "artistid") {
    val name: Column<String> = varchar("name", 120)
}

object Album : IntIdTable("album", "albumid") {
    val title: Column<String> = varchar("title", 160)
    val artistId: Column<Int> = integer("artistid").references(Artist.id)
}

object Track : IntIdTable("track", "trackid") {
    val name: Column<String> = varchar("name", 200)
    val albumId: Column<Int> = integer("albumid").references(Album.id)
    val mediaTypeId: Column<Int> = integer("mediatypeid").references(MediaType.id)
    val genreId: Column<Int> = integer("genreid").references(Genre.id)
    val composer: Column<String> = varchar("composer", 220)
    val milliseconds: Column<Int> = integer("milliseconds")
    val bytes: Column<Int> = integer("bytes")
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

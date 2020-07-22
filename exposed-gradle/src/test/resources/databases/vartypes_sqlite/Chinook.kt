package databases.vartypes_sqlite

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.datetime
import java.math.BigDecimal
import java.time.LocalDateTime

object Playlist : Table("playlist") {
    val playlistId: Column<Int> = integer("playlistid")
    val name: Column<String?> = varchar("name", 120).nullable()

    override val primaryKey: PrimaryKey = PrimaryKey(playlistId)
}

object MediaType : Table("mediatype") {
    val mediaTypeId: Column<Int> = integer("mediatypeid")
    val name: Column<String?> = varchar("name", 120).nullable()

    override val primaryKey: PrimaryKey = PrimaryKey(mediaTypeId)
}

object Genre : Table("genre") {
    val genreId: Column<Int> = integer("genreid")
    val name: Column<String?> = varchar("name", 120).nullable()

    override val primaryKey: PrimaryKey = PrimaryKey(genreId)
}

object Employee : Table("employee") {
    val employeeId: Column<Int> = integer("employeeid")
    val lastName: Column<String> = varchar("lastname", 20)
    val firstName: Column<String> = varchar("firstname", 20)
    val title: Column<String?> = varchar("title", 30).nullable()
    val reportsTo: Column<Int?> = integer("reportsto").references(employeeId).nullable()
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

    override val primaryKey: PrimaryKey = PrimaryKey(employeeId)
}

object Customer : Table("customer") {
    val customerId: Column<Int> = integer("customerid")
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
            integer("supportrepid").references(Employee.employeeId).nullable()

    override val primaryKey: PrimaryKey = PrimaryKey(customerId)
}

object Invoice : Table("invoice") {
    val invoiceId: Column<Int> = integer("invoiceid")
    val customerId: Column<Int> = integer("customerid").references(Customer.customerId)
    val invoiceDate: Column<LocalDateTime> = datetime("invoicedate")
    val billingAddress: Column<String?> = varchar("billingaddress", 70).nullable()
    val billingCity: Column<String?> = varchar("billingcity", 40).nullable()
    val billingState: Column<String?> = varchar("billingstate", 40).nullable()
    val billingCountry: Column<String?> = varchar("billingcountry", 40).nullable()
    val billingPostalCode: Column<String?> = varchar("billingpostalcode", 10).nullable()
    val total: Column<BigDecimal> = decimal("total", 10, 2)

    override val primaryKey: PrimaryKey = PrimaryKey(invoiceId)
}

object Artist : Table("artist") {
    val artistId: Column<Int> = integer("artistid")
    val name: Column<String?> = varchar("name", 120).nullable()

    override val primaryKey: PrimaryKey = PrimaryKey(artistId)
}

object Album : Table("album") {
    val albumId: Column<Int> = integer("albumid")
    val title: Column<String> = varchar("title", 160)
    val artistId: Column<Int> = integer("artistid").references(Artist.artistId)

    override val primaryKey: PrimaryKey = PrimaryKey(albumId)
}

object Track : Table("track") {
    val trackId: Column<Int> = integer("trackid")
    val name: Column<String> = varchar("name", 200)
    val albumId: Column<Int?> = integer("albumid").references(Album.albumId).nullable()
    val mediaTypeId: Column<Int> = integer("mediatypeid").references(MediaType.mediaTypeId)
    val genreId: Column<Int?> = integer("genreid").references(Genre.genreId).nullable()
    val composer: Column<String?> = varchar("composer", 220).nullable()
    val milliseconds: Column<Int> = integer("milliseconds")
    val bytes: Column<Int?> = integer("bytes").nullable()
    val unitPrice: Column<BigDecimal> = decimal("unitprice", 10, 2)

    override val primaryKey: PrimaryKey = PrimaryKey(trackId)
}

object InvoiceLine : Table("invoiceline") {
    val invoiceLineId: Column<Int> = integer("invoicelineid")
    val invoiceId: Column<Int> = integer("invoiceid").references(Invoice.invoiceId)
    val trackId: Column<Int> = integer("trackid").references(Track.trackId)
    val unitPrice: Column<BigDecimal> = decimal("unitPrice", 10, 2)
    val quantity: Column<Int> = integer("quantity")

    override val primaryKey: PrimaryKey = PrimaryKey(invoiceLineId)
}

object PlaylistTrack : Table("playlisttrack") {
    val playlistId: Column<Int> = integer("playlistid").references(Playlist.playlistId)
    val trackId: Column<Int> = integer("trackid").references(Track.trackId)

    override val primaryKey: PrimaryKey = PrimaryKey(playlistId, trackId)
}

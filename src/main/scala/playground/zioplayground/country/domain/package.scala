package playground.zioplayground.country

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

package object domain {

  final case class CountryCode(code: String) extends AnyVal

  object CountryCode {
    implicit val encoder: Encoder[CountryCode] = deriveEncoder
    implicit val decoder: Decoder[CountryCode] = deriveDecoder
  }

  final case class Country(name: String,
                           countryCode: CountryCode,
                           continent: String,
                           region: String) {
    def update(patchForm: CountryPatchForm): Country =
      this.copy(
        name = patchForm.name.getOrElse(name),
        region = patchForm.region.getOrElse(region)
      )
  }

  object Country {
    implicit val encoder: Encoder[Country] = deriveEncoder
    implicit val decoder: Decoder[Country] = deriveDecoder
  }

  final case class CountryPostForm(name: String,
                                   code: String,
                                   continent: String,
                                   region: String) {
    def toCountry: Country = Country(
      name,
      CountryCode(code),
      continent,
      region
    )
  }

  object CountryPostForm {
    implicit val encoder: Encoder[CountryPostForm] = deriveEncoder
    implicit val decoder: Decoder[CountryPostForm] = deriveDecoder
  }

  final case class CountryPatchForm(name: Option[String],
                                    region: Option[String])

  object CountryPatchForm {
    implicit val encoder: Encoder[CountryPatchForm] = deriveEncoder
    implicit val decoder: Decoder[CountryPatchForm] = deriveDecoder
  }

}

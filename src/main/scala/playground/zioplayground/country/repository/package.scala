package playground.zioplayground.country

import zio.Has

package object repository {
  type CountryRepository = Has[CountryRepository.Service]
}

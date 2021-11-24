package playground.zioplayground.utils

import org.http4s.{Method, Request, Uri}

object HTTPSpec {

  def request[F[_]](
                     method: Method,
                     uri: String
                   ): Request[F] =
    Request[F](method = method, uri = Uri.fromString(uri).toOption.get)

}

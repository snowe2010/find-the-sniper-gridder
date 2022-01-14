import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.tylerthrailkill.sniper.FindTheSniperService
import javax.inject.Inject
import javax.inject.Named

class A

@Named("LambdaHandler")
class LambdaHandler @Inject constructor(
    val findTheSniperService: FindTheSniperService,
) : RequestHandler<Unit, A> {

    override fun handleRequest(request: Unit, context: Context?): A {
        findTheSniperService.commentOnNewPosts()
        return A() // have to return object to avoid weird serialization error...
    }
}

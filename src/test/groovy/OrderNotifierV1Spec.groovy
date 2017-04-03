
import com.example.EmailService
import com.example.Location
import com.example.Order
import com.example.OrderRepository
import com.example.OrdersNotifierV1
import com.example.Region
import com.example.Role
import com.example.SalesReportRepository
import com.example.User
import com.example.UserRepository
import spock.lang.*

class OrderNotifierV1Spec extends Specification {
    def userRepository = Mock(UserRepository)
    def emailService = Mock(EmailService)
    def orderRepository = Stub(OrderRepository)
    def salesReportRepository = Stub(SalesReportRepository)
    def notifier = new OrdersNotifierV1(userRepository, orderRepository, salesReportRepository, emailService);
    def location = Stub(Location)
    def region = Stub(Region)

    def LOCAL_OBJECTIVE = 1000
    def REGIONAL_OBJECTIVE = 5000

    def setup() {
        salesReportRepository.getLocalSalesObjective(_) >> LOCAL_OBJECTIVE
        salesReportRepository.getRegionalSalesObjective(_) >> REGIONAL_OBJECTIVE
        location.getName() >> "Laval"
        location.getRegion() >> region
        region.getName() >> "Montreal"
    }

    def "notifier sends email to order creator"() {
        setup:
        def order = new Order(new User("Bob", "bob@hotmail.com"), 100, location)
        orderRepository.findAll(_) >> [order]

        when:
        notifier.onNewOrder(order);

        then:
        //threshold not met so there should be no call to the user repository
        0 * userRepository.findAll()
        //email sent only to the salesman
        1 * emailService.sendMail(*_) >> { arguments ->
            Collection<String> emails = arguments[2]
            assert emails.size() == 1
            assert emails[0] == "bob@hotmail.com"

            String body = arguments[1]
            assert body.startsWith("Hi Bob")
            assert body.contains("The total of you order is 100")
        }
    }

    def "notifier sends email to local managers when order amount is over threshold"() {
        setup:
        def order1 = new Order(new User("Bob", "bob@hotmail.com"), LOCAL_OBJECTIVE/2, location)
        def order2 = new Order(new User("Bob", "bob@hotmail.com"), LOCAL_OBJECTIVE/2 + 1, location)
        orderRepository.findAll(_) >> [order1, order2]

        when:
        notifier.onNewOrder(order2);

        then:
        1 * userRepository.findAll() >> [new User("Ray", "ray@hotmail.com", Role.LOCAL_MANAGER), new User("Jay", "jay@hotmail.com", Role.REGIONAL_MANAGER)]
        1 * emailService.sendMail(*_) >> { arguments ->
            Collection<String> emails = arguments[2]
            assert emails.size() == 1
            assert emails[0] == "bob@hotmail.com"
        }

        then:
        1 * emailService.sendMail(*_) >> { arguments ->
            Collection<String> emails = arguments[2]
            assert emails.size() == 1
            assert emails[0] == "ray@hotmail.com"
        }
    }

    def "notifier sends email to regional managers when order amount is over threshold"() {
        setup:
        def order = new Order(new User("Bob", "bob@hotmail.com"), REGIONAL_OBJECTIVE, location)
        orderRepository.findAll(_) >> [order]

        when:
        notifier.onNewOrder(order);

        then:
        2 * userRepository.findAll() >> [new User("Ray", "ray@hotmail.com", Role.LOCAL_MANAGER), new User("Jay", "jay@hotmail.com", Role.REGIONAL_MANAGER)]
        1 * emailService.sendMail(*_) >> { arguments ->
            Collection<String> emails = arguments[2]
            assert emails.size() == 1
            assert emails[0] == "bob@hotmail.com"
        }
        1 * emailService.sendMail(*_) >> { arguments ->
            Collection<String> emails = arguments[2]
            assert emails.size() == 1
            assert emails[0] == "ray@hotmail.com"
        }
        1 * emailService.sendMail(*_) >> { arguments ->
            Collection<String> emails = arguments[2]
            assert emails.size() == 1
            assert emails[0] == "jay@hotmail.com"
        }
    }

}
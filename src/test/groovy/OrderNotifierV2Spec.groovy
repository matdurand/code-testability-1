import com.example.*
import spock.lang.Specification

class OrderNotifierV2Spec extends Specification {
    def userRepository = Stub(UserRepository)
    def emailService = Mock(EmailService)
    def salesThresholdResolver = Stub(SalesThresholdResolver)
    def notifier = new OrdersNotifierV2(userRepository, salesThresholdResolver, emailService);

    def setup() {
        userRepository.findAll() >> [new User("Ray", "ray@hotmail.com", Role.LOCAL_MANAGER), new User("Jay", "jay@hotmail.com", Role.REGIONAL_MANAGER)]
    }

    def assertOneEmailTarget(arguments, email) {
        Collection<String> emails = arguments[2]
        assert emails.size() == 1
        assert emails[0] == email
    }

    def "notifier sends email to order creator"() {
        setup:
        salesThresholdResolver.isLocalSalesThresholdMetWithNewOrder(_) >> false
        salesThresholdResolver.isRegionalSalesThresholdMetWithNewOrder(_) >> false

        when:
        notifier.onNewOrder(new Order(new User("Bob", "bob@hotmail.com"), 100, null));

        then:
        //email sent only to the salesman
        1 * emailService.sendMail(*_) >> { arguments ->
            assertOneEmailTarget(arguments, "bob@hotmail.com")
            String body = arguments[1]
            assert body.startsWith("Hi Bob")
            assert body.contains("The total of you order is 100")
        }
    }

    def "notifier sends email to local managers when local threshold resolver returns true"() {
        setup:
        salesThresholdResolver.isLocalSalesThresholdMetWithNewOrder(_) >> true
        salesThresholdResolver.isRegionalSalesThresholdMetWithNewOrder(_) >> false

        when:
        notifier.onNewOrder(new Order(new User("Bob", "bob@hotmail.com"), 100, null));

        then:
        1 * emailService.sendMail(*_) >> { arguments ->
            assertOneEmailTarget(arguments, "bob@hotmail.com")
        }
        1 * emailService.sendMail(*_) >> { arguments ->
            assertOneEmailTarget(arguments, "ray@hotmail.com")
        }
    }

    def "notifier sends email to local managers when regional threshold resolver returns true"() {
        setup:
        salesThresholdResolver.isLocalSalesThresholdMetWithNewOrder(_) >> false
        salesThresholdResolver.isRegionalSalesThresholdMetWithNewOrder(_) >> true

        when:
        notifier.onNewOrder(new Order(new User("Bob", "bob@hotmail.com"), 100, null));

        then:
        1 * emailService.sendMail(*_) >> { arguments ->
            assertOneEmailTarget(arguments, "bob@hotmail.com")
        }
        1 * emailService.sendMail(*_) >> { arguments ->
            assertOneEmailTarget(arguments, "jay@hotmail.com")
        }
    }

}
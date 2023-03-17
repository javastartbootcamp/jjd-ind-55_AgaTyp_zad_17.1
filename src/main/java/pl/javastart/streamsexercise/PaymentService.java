package pl.javastart.streamsexercise;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class PaymentService {

    private final PaymentRepository paymentRepository;
    private final DateTimeProvider dateTimeProvider;

    PaymentService(PaymentRepository paymentRepository, DateTimeProvider dateTimeProvider) {
        this.paymentRepository = paymentRepository;
        this.dateTimeProvider = dateTimeProvider;
    }

    /*
    Znajdź i zwróć płatności posortowane po dacie rosnąco
     */
    List<Payment> findPaymentsSortedByDateAsc() {
        return paymentRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Payment::getPaymentDate))
                .toList();
    }

    /*
    Znajdź i zwróć płatności posortowane po dacie malejąco
     */
    List<Payment> findPaymentsSortedByDateDesc() {
        return paymentRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Payment::getPaymentDate).reversed())
                .toList();
    }

    /*
    Znajdź i zwróć płatności posortowane po liczbie elementów rosnąco
     */
    List<Payment> findPaymentsSortedByItemCountAsc() {
        return paymentRepository.findAll()
                .stream()
                .sorted(Comparator.comparingInt(p -> p.getPaymentItems().size()))
                .toList();

    }

    /*
    Znajdź i zwróć płatności posortowane po liczbie elementów malejąco
     */
    List<Payment> findPaymentsSortedByItemCountDesc() {
        return paymentRepository.findAll()
                .stream()
                .sorted((p1, p2) -> Integer.compare(p2.getPaymentItems().size(), p1.getPaymentItems().size()))
                .toList();

    }

    /*
    Znajdź i zwróć płatności dla wskazanego miesiąca
     */
    List<Payment> findPaymentsForGivenMonth(YearMonth yearMonth) {
        return paymentRepository.findAll()
                .stream()
                .filter(payment -> isSpecificMonth(yearMonth, payment))
                .toList();
    }

    /*
    Znajdź i zwróć płatności dla aktualnego miesiąca
     */
    List<Payment> findPaymentsForCurrentMonth() {
        return paymentRepository.findAll()
                .stream()
                .filter(payment -> isSpecificMonth(dateTimeProvider.yearMonthNow(), payment))
                .toList();
    }

    /*
    Znajdź i zwróć płatności dla ostatnich X dni
     */
    List<Payment> findPaymentsForGivenLastDays(int days) {
        return paymentRepository.findAll()
                .stream()
                .filter(payment -> (payment.getPaymentDate().isBefore(dateTimeProvider.zonedDateTimeNow())
                        && payment.getPaymentDate().isAfter(dateTimeProvider.zonedDateTimeNow().minusDays(days))))
                .toList();
    }

    /*
    Znajdź i zwróć płatności z jednym elementem
     */
    Set<Payment> findPaymentsWithOnePaymentItem() {
        return paymentRepository.findAll()
                .stream()
                .filter(payment -> payment.getPaymentItems().size() == 1)
                .collect(Collectors.toSet());
    }

    /*
    Znajdź i zwróć nazwy produktów sprzedanych w aktualnym miesiącu
     */
    Set<String> findProductsSoldInCurrentMonth() {
        return paymentRepository.findAll()
                .stream()
                .filter(payment -> isSpecificMonth(dateTimeProvider.yearMonthNow(), payment))
                .map(Payment::getPaymentItems)
                .flatMap(Collection::stream)
                .map(PaymentItem::getName)
                .collect(Collectors.toSet());

    }

    /*
    Policz i zwróć sumę sprzedaży dla wskazanego miesiąca
     */
    BigDecimal sumTotalForGivenMonth(YearMonth yearMonth) {
        return paymentRepository.findAll()
                .stream()
                .filter(payment -> isSpecificMonth(yearMonth, payment))
                .map(Payment::getPaymentItems)
                .flatMap(Collection::stream)
                .map(PaymentItem::getFinalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /*
    Policz i zwróć sumę przyznanych rabatów dla wskazanego miesiąca
     */
    BigDecimal sumDiscountForGivenMonth(YearMonth yearMonth) {
        return paymentRepository.findAll()
                .stream()
                .filter(payment -> isSpecificMonth(yearMonth, payment))
                .map(Payment::getPaymentItems)
                .flatMap(Collection::stream)
                .map(PaymentItem::getDiscount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /*
    Znajdź i zwróć płatności dla użytkownika z podanym mailem
     */
    List<PaymentItem> getPaymentsForUserWithEmail(String userEmail) {
        return paymentRepository.findAll()
                .stream()
                .filter(payment -> payment.getUser().getEmail().equals(userEmail))
                .map(Payment::getPaymentItems)
                .flatMap(Collection::stream)
                .toList();
    }

    /*
    Znajdź i zwróć płatności, których wartość przekracza wskazaną granicę
     */
    Set<Payment> findPaymentsWithValueOver(int value) {
        return paymentRepository.findAll()
                .stream()
                .filter(payment -> getPrice(payment.getPaymentItems()) > value)
                .collect(Collectors.toSet());
    }

    private Integer getPrice(List<PaymentItem> paymentItems) {
        int sum = 0;
        for (PaymentItem item : paymentItems) {
            sum += Integer.parseInt(String.valueOf(item.getFinalPrice()));
        }
        return sum;
    }

    private static boolean isSpecificMonth(YearMonth yearMonth, Payment payment) {
        return payment.getPaymentDate().getMonth() == yearMonth.getMonth()
                && payment.getPaymentDate().getYear() == yearMonth.getYear();
    }

}

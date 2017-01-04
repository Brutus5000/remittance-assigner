package biz.schmidt_it.remittanceassigner;

import javafx.util.StringConverter;
import org.javamoney.moneta.Money;
import org.javamoney.moneta.format.CurrencyStyle;

import javax.money.MonetaryAmount;
import javax.money.format.AmountFormatQueryBuilder;
import javax.money.format.MonetaryAmountFormat;
import javax.money.format.MonetaryFormats;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

public class Util {
    private Util() {}

    public static MonetaryAmount tryParse(String string){
        try {
            string = string.replaceAll("([0-9.,])([a-zA-Z\\p{Sc}])", "$1 $2");
            MonetaryAmountFormat fmt = MonetaryFormats.getAmountFormat(
                    AmountFormatQueryBuilder.of(Locale.GERMANY)
                            .set(CurrencyStyle.SYMBOL)
                            .build());

            return fmt.parse(string);
        } catch (Exception e) {
            try {
                MonetaryAmountFormat fmt = MonetaryFormats.getAmountFormat(
                        AmountFormatQueryBuilder.of(Locale.US)
                                .set(CurrencyStyle.SYMBOL)
                                .build());

                return fmt.parse(string);
            } catch (Exception e2) {
                try {
                    Locale locale = Locale.getDefault();
                    NumberFormat nf = NumberFormat.getInstance(locale);
                    return Money.of(nf.parse(string), Currency.getInstance(locale).toString());
                } catch (Exception e3) {
                    throw new RuntimeException(string+" can not be parsed as a valid amount of money.");
                }
            }
        }
    }

    public static StringConverter<MonetaryAmount> getMonetaryAmountToStringConverter(MonetaryAmountFormat formatter) {
        return new StringConverter<MonetaryAmount>() {
            @Override
            public String toString(MonetaryAmount object) {
                return object == null ? "" : formatter.format(object);
            }

            @Override
            public MonetaryAmount fromString(String string) {
                return Util.tryParse(string);
            }
        };
    }
}

package tech.zerofiltre.testing.calcul.service;

import javax.inject.Named;
import java.util.Locale;

@Named
public class SolutionFormatterImpl implements SolutionFormatter {

	@Override
	public String format(int solution) {

		return String.format(Locale.US, "%,d", solution)
				.replace(",", "\u00A0");
	}
}
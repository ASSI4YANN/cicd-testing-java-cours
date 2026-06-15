package tech.zerofiltre.testing.calcul.service;

import javax.inject.Named;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

@Named
public class SolutionFormatterImpl implements SolutionFormatter {

	@Override
	public String format(int solution) {
		DecimalFormatSymbols symbols = new DecimalFormatSymbols();

		// IMPORTANT: espace insécable standard attendu par ton test
		symbols.setGroupingSeparator('\u00A0');

		DecimalFormat formatter = new DecimalFormat("#,###", symbols);

		return formatter.format(solution);
	}
}
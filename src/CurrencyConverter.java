import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Scanner;

public class CurrencyConverter {

    public static void main(String[] args) {
        HashMap<Integer, String> currencyCodes = new HashMap<>();
        currencyCodes.put(1, "USD");
        currencyCodes.put(2, "EUR");
        currencyCodes.put(3, "HKD");
        currencyCodes.put(4, "INR");
        currencyCodes.put(5, "FRW");
        currencyCodes.put(6, "CAD");

        String fromCode, toCode;
        double amount;

        Scanner userInput = new Scanner(System.in);

        System.out.println("Welcome to the currency converter.");
        System.out.println("Please enter the currency code you would like to convert from:");
        System.out.println("1: USD (US Dollars)\n2: EUR (Euro)\n3: HKD (Hong Kong Dollars)\n4: INR (Indian Rupees)\n5: FRW (Rwandan Francs)\n6: CAD (Canadian Dollars)");
        fromCode = currencyCodes.get(userInput.nextInt());

        System.out.println("Please enter the currency code you would like to convert to:");
        System.out.println("1: USD (US Dollars)\n2: EUR (Euro)\n3: HKD (Hong Kong Dollars)\n4: INR (Indian Rupees)\n5: FRW (Rwandan Francs)\n6: CAD (Canadian Dollars)");
        toCode = currencyCodes.get(userInput.nextInt());

        System.out.println("Enter the amount you would like to convert:");
        amount = userInput.nextDouble();

        try {
            double convertedAmount = convertCurrency(fromCode, toCode, amount);
            System.out.printf("%.2f %s = %.2f %s\n", amount, fromCode, convertedAmount, toCode);
        } catch (IOException e) {
            System.out.println("An error occurred while converting currencies: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
        } finally {
            userInput.close();
            System.out.println("Thank you for using Currency Converter");
        }
    }

    private static double convertCurrency(String fromCode, String toCode, double amount) throws IOException {
        String GET_URL = "https://api.exchangeratesapi.io/latest?base=" + fromCode + "&symbols=" + toCode + "&access_key=f962fd79510d805c77a43ea8";

        URL url = new URL(GET_URL);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod("GET");

        int responseCode = httpURLConnection.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;

            while ((inputLine = bufferedReader.readLine()) != null) {
                response.append(inputLine);
            }

            bufferedReader.close();

            // Parse JSON response to get exchange rate
            double exchangeRate = parseExchangeRate(response.toString(), toCode);
            return amount * exchangeRate;
        } else {
            throw new IOException("HTTP error code: " + responseCode);
        }
    }

    private static double parseExchangeRate(String jsonResponse, String toCode) throws IOException {
        // Assuming the response JSON format is similar to {"rates":{"USD":1.234}}
        // Extract exchange rate for the target currency 'toCode'
        double exchangeRate = 0.0;
        try {
            // Check if the JSON response contains the rates section
            if (jsonResponse.contains("rates")) {
                int startIndex = jsonResponse.indexOf("rates");
                if (startIndex == -1) {
                    throw new IOException("No rates section found in the response.");
                }
                String ratesSection = jsonResponse.substring(startIndex + 7, jsonResponse.indexOf("}", startIndex));
                String rateString = ratesSection.substring(ratesSection.indexOf("\"" + toCode + "\":") + toCode.length() + 4, ratesSection.indexOf(",", ratesSection.indexOf("\"" + toCode + "\":")));
                exchangeRate = Double.parseDouble(rateString.trim());
            } else {
                throw new IOException("No rates section found in the response.");
            }
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            throw new IOException("Error parsing exchange rate: " + e.getMessage());
        }
        return exchangeRate;
    }
}

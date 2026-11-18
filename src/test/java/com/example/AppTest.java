
package com.example;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class App {

    // Calculator operations
    public double add(double a, double b) {
        return a + b;
    }

    public double subtract(double a, double b) {
        return a - b;
    }

    public double multiply(double a, double b) {
        return a * b;
    }

    public double divide(double a, double b) {
        if (b == 0) {
            throw new ArithmeticException("Cannot divide by zero");
        }
        return a / b;
    }

    // Start Java web server
    public static void main(String[] args) throws IOException {

        HttpServer server = HttpServer.create(
                new InetSocketAddress("0.0.0.0", 8080), 0);

        server.createContext("/", exchange -> {

            if (exchange.getRequestURI().getPath().equals("/calculate")) {
                handleCalculation(exchange);
            } else {
                serveFrontend(exchange);
            }
        });

        server.setExecutor(null);
        server.start();

        System.out.println("Calculator website running on port 8080");
    }

    // Serve frontend website
    private static void serveFrontend(HttpExchange exchange)
            throws IOException {

        String html = """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Java Calculator</title>

            <style>
                * {
                    box-sizing: border-box;
                }

                body {
                    margin: 0;
                    min-height: 100vh;
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    font-family: Arial, sans-serif;
                    background: linear-gradient(135deg, #101827, #263b65);
                    color: white;
                }

                .container {
                    width: 90%;
                    max-width: 450px;
                    padding: 35px;
                    background: #1c2940;
                    border-radius: 22px;
                    box-shadow: 0 15px 45px #0007;
                }

                h1 {
                    text-align: center;
                    color: #60a5fa;
                    margin-bottom: 8px;
                }

                .subtitle {
                    text-align: center;
                    color: #aabbd3;
                    margin-bottom: 30px;
                }

                label {
                    display: block;
                    margin: 15px 0 8px;
                    color: #cbd5e1;
                }

                input, select {
                    width: 100%;
                    padding: 14px;
                    border: 1px solid #405574;
                    border-radius: 10px;
                    background: #101827;
                    color: white;
                    font-size: 16px;
                    outline: none;
                }

                button {
                    width: 100%;
                    padding: 15px;
                    margin-top: 25px;
                    border: none;
                    border-radius: 10px;
                    background: #3b82f6;
                    color: white;
                    font-size: 17px;
                    font-weight: bold;
                    cursor: pointer;
                }

                button:hover {
                    background: #2563eb;
                }

                .result {
                    margin-top: 25px;
                    padding: 20px;
                    border-radius: 12px;
                    background: #101827;
                    text-align: center;
                }

                .result h3 {
                    color: #60a5fa;
                    margin: 0 0 12px;
                }

                #answer {
                    font-size: 27px;
                    font-weight: bold;
                    overflow-wrap: anywhere;
                }

                .footer {
                    margin-top: 25px;
                    text-align: center;
                    color: #94a3b8;
                    font-size: 12px;
                }
            </style>
        </head>

        <body>

        <div class="container">

            <h1>Java Calculator</h1>
            <p class="subtitle">Simple and Smart Calculator</p>

            <label>First Number</label>
            <input type="number" id="num1" placeholder="Enter first number" step="any">

            <label>Operation</label>
            <select id="operation">
                <option value="add">Addition (+)</option>
                <option value="subtract">Subtraction (-)</option>
                <option value="multiply">Multiplication (×)</option>
                <option value="divide">Division (÷)</option>
            </select>

            <label>Second Number</label>
            <input type="number" id="num2" placeholder="Enter second number" step="any">

            <button onclick="calculate()">Calculate Result</button>

            <div class="result">
                <h3>Result</h3>
                <div id="answer">Your answer appears here</div>
            </div>

            <div class="footer">
                Built with Java 17 | Maven | GitHub Actions
            </div>

        </div>

        <script>
            async function calculate() {

                const a = document.getElementById("num1").value;
                const b = document.getElementById("num2").value;
                const operation = document.getElementById("operation").value;
                const answer = document.getElementById("answer");

                if (a === "" || b === "") {
                    answer.innerText = "Please enter both numbers";
                    return;
                }

                try {
                    const params = new URLSearchParams({
                        a: a,
                        b: b,
                        operation: operation
                    });

                    const response = await fetch("/calculate?" + params);

                    const data = await response.json();

                    if (!response.ok) {
                        throw new Error(data.error);
                    }

                    answer.innerText = data.result;

                } catch (error) {
                    answer.innerText = error.message;
                }
            }
        </script>

        </body>
        </html>
        """;

        byte[] response = html.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set(
                "Content-Type", "text/html; charset=UTF-8");

        exchange.sendResponseHeaders(200, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }

    // Handle calculator requests using Java backend
    private static void handleCalculation(HttpExchange exchange)
            throws IOException {

        try {
            Map<String, String> params = new HashMap<>();

            String query = exchange.getRequestURI().getRawQuery();

            if (query != null) {
                for (String pair : query.split("&")) {
                    String[] parts = pair.split("=", 2);

                    if (parts.length == 2) {
                        params.put(
                                URLDecoder.decode(parts[0], StandardCharsets.UTF_8),
                                URLDecoder.decode(parts[1], StandardCharsets.UTF_8)
                        );
                    }
                }
            }

            double a = Double.parseDouble(params.get("a"));
            double b = Double.parseDouble(params.get("b"));

            App calculator = new App();

            double result = switch (params.get("operation")) {
                case "add" -> calculator.add(a, b);
                case "subtract" -> calculator.subtract(a, b);
                case "multiply" -> calculator.multiply(a, b);
                case "divide" -> calculator.divide(a, b);
                default -> throw new IllegalArgumentException("Invalid operation");
            };

            sendJson(exchange, 200,
                    "{\"result\":" + result + "}");

        } catch (Exception e) {
            sendJson(exchange, 400,
                    "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private static String escapeJson(String value) {
        if (value == null) return "Unknown error";
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static void sendJson(
            HttpExchange exchange, int status, String json)
            throws IOException {

        byte[] response = json.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set(
                "Content-Type", "application/json; charset=UTF-8");

        exchange.sendResponseHeaders(status, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }
}


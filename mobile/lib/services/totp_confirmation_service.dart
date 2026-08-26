import 'dart:convert';

import 'package:http/http.dart' as http;

class TotpConfirmationService {
  static const String baseUrl = 'https://totp-auth-backend.onrender.com';

  Future<void> confirm({
    required String accessToken,
    required String code,
  }) async {
    final response = await http.post(
      Uri.parse('$baseUrl/api/totp/confirm'),
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer $accessToken',
      },
      body: jsonEncode({
        'code': code,
      }),
    );

    if (response.statusCode >= 200 &&
        response.statusCode < 300) {
      return;
    }

    String message = 'TOTP verification failed.';

    try {
      final Map<String, dynamic> data =
          jsonDecode(response.body)
              as Map<String, dynamic>;

      message =
          data['message']?.toString() ?? message;
    } catch (_) {
      // Keep the default message when the response
      // doesn't contain JSON.
    }

    throw Exception(message);
  }
}
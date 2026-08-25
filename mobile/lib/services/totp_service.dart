import 'dart:convert';

import 'package:http/http.dart' as http;

import '../models/totp_enrollment_response.dart';

class TotpService {
  static const String baseUrl = 'http://localhost:8080';

  Future<TotpEnrollmentResponse> enroll({
    required String accessToken,
  }) async {
    final response = await http.post(
      Uri.parse('$baseUrl/api/totp/enroll'),
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer $accessToken',
      },
    );

    final Map<String, dynamic> data =
        jsonDecode(response.body) as Map<String, dynamic>;

    if (response.statusCode >= 200 &&
        response.statusCode < 300) {
      return TotpEnrollmentResponse.fromJson(data);
    }

    throw Exception(
      data['message']?.toString() ??
          'TOTP enrollment failed',
    );
  }
}

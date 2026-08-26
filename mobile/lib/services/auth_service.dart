import 'dart:convert';

import 'package:http/http.dart' as http;

import '../models/login_response.dart';

class AuthService {
  static const String baseUrl =
    'https://totp-auth-backend.onrender.com';
  Future<LoginResponse> login({
    required String identifier,
    required String password,
  }) async {
    final response = await http.post(
      Uri.parse('$baseUrl/api/auth/login'),
      headers: {
        'Content-Type': 'application/json',
        'X-Device-ID': 'flutter-web-device',
        'X-Device-Name': 'Flutter Web',
      },
      body: jsonEncode({
        'identifier': identifier,
        'password': password,
      }),
    );

    final Map<String, dynamic> data =
        jsonDecode(response.body) as Map<String, dynamic>;

    if (response.statusCode >= 200 &&
        response.statusCode < 300) {
      return LoginResponse.fromJson(data);
    }

    throw Exception(
      data['message']?.toString() ?? 'Login failed',
    );
  }

  Future<OtpVerificationResponse> verifyOtp({
    required String tempAuthSessionId,
    required String otp,
  }) async {
    final response = await http.post(
      Uri.parse('$baseUrl/api/auth/verify-otp'),
      headers: {
        'Content-Type': 'application/json',
        'X-Device-ID': 'flutter-web-device',
        'X-Device-Name': 'Flutter Web',
      },
      body: jsonEncode({
        'tempAuthSessionId': tempAuthSessionId,
        'otp': otp,
      }),
    );

    final Map<String, dynamic> data =
        jsonDecode(response.body) as Map<String, dynamic>;

    if (response.statusCode >= 200 &&
        response.statusCode < 300) {
      return OtpVerificationResponse.fromJson(data);
    }

    throw Exception(
      data['errorMessage']?.toString() ??
          data['message']?.toString() ??
          'OTP verification failed',
    );
  }

  Future<RegisterResponse> register({
    required String userId,
    required String email,
    required String password,
  }) async {
    final response = await http.post(
      Uri.parse('$baseUrl/api/auth/register'),
      headers: {
        'Content-Type': 'application/json',
      },
      body: jsonEncode({
        'userId': userId,
        'email': email,
        'password': password,
      }),
    );

    final Map<String, dynamic> data =
        jsonDecode(response.body) as Map<String, dynamic>;

    if (response.statusCode >= 200 &&
        response.statusCode < 300) {
      return RegisterResponse.fromJson(data);
    }

    throw Exception(
      data['message']?.toString() ??
          data['errorMessage']?.toString() ??
          'Registration failed',
    );
  }

  // ============================================================
  // EXISTING TOTP PASSWORD RESET FLOW
  // ============================================================

  Future<PasswordResetResponse> requestPasswordReset({
    required String identifier,
  }) async {
    final response = await http.post(
      Uri.parse(
        '$baseUrl/api/auth/password-reset/request',
      ),
      headers: {
        'Content-Type': 'application/json',
      },
      body: jsonEncode({
        'identifier': identifier,
      }),
    );

    final Map<String, dynamic> data =
        jsonDecode(response.body)
            as Map<String, dynamic>;

    if (response.statusCode >= 200 &&
        response.statusCode < 300) {
      return PasswordResetResponse.fromJson(data);
    }

    throw Exception(
      data['message']?.toString() ??
          'Password recovery request failed',
    );
  }

  Future<PasswordResetResponse> verifyPasswordReset({
    required String resetToken,
    required String totpCode,
  }) async {
    final response = await http.post(
      Uri.parse(
        '$baseUrl/api/auth/password-reset/verify',
      ),
      headers: {
        'Content-Type': 'application/json',
      },
      body: jsonEncode({
        'resetToken': resetToken,
        'totpCode': totpCode,
      }),
    );

    final Map<String, dynamic> data =
        jsonDecode(response.body)
            as Map<String, dynamic>;

    if (response.statusCode >= 200 &&
        response.statusCode < 300) {
      return PasswordResetResponse.fromJson(data);
    }

    throw Exception(
      data['message']?.toString() ??
          'TOTP verification failed',
    );
  }

  Future<PasswordResetResponse> completePasswordReset({
    required String resetToken,
    required String newPassword,
  }) async {
    final response = await http.post(
      Uri.parse(
        '$baseUrl/api/auth/password-reset/complete',
      ),
      headers: {
        'Content-Type': 'application/json',
      },
      body: jsonEncode({
        'resetToken': resetToken,
        'newPassword': newPassword,
      }),
    );

    final Map<String, dynamic> data =
        jsonDecode(response.body)
            as Map<String, dynamic>;

    if (response.statusCode >= 200 &&
        response.statusCode < 300) {
      return PasswordResetResponse.fromJson(data);
    }

    throw Exception(
      data['message']?.toString() ??
          'Password reset failed',
    );
  }

  // ============================================================
  // EMAIL PASSWORD RECOVERY FLOW
  // ============================================================


  Future<PasswordRecoveryVerificationResponse>
      verifyPasswordRecovery({
    required String identifier,
    required String code,
  }) async {
    final response = await http.post(
      Uri.parse(
        '$baseUrl/api/auth/password-recovery/verify',
      ),
      headers: {
        'Content-Type': 'application/json',
      },
      body: jsonEncode({
        'identifier': identifier,
        'code': code,
      }),
    );

    Map<String, dynamic> data = {};

    if (response.body.isNotEmpty) {
      try {
        data =
            jsonDecode(response.body)
                as Map<String, dynamic>;
      } catch (_) {
        throw Exception(
          'Invalid server response',
        );
      }
    }

    if (response.statusCode >= 200 &&
        response.statusCode < 300) {
      return PasswordRecoveryVerificationResponse
          .fromJson(data);
    }

    throw Exception(
      data['errorMessage']?.toString() ??
          data['message']?.toString() ??
          _passwordRecoveryError(
            data['errorCode']?.toString(),
          ),
    );
  }

  String _passwordRecoveryError(
    String? errorCode,
  ) {
    switch (errorCode) {
      case 'INVALID_CODE':
        return 'Incorrect verification code.';

      case 'EXPIRED':
        return 'The verification code has expired.';

      case 'LOCKED':
        return 'Too many incorrect attempts.';

      case 'TOTP_ENABLED':
        return 'Use your authenticator app to recover your password.';

      case 'INVALID_REQUEST':
        return 'Invalid password recovery request.';

      default:
        return 'Email verification failed.';
    }
  }
}

// ================================================================
// LOGIN / TOTP RESPONSE
// ================================================================

class OtpVerificationResponse {
  final String status;
  final String accessToken;
  final String refreshToken;
  final int expiresIn;

  const OtpVerificationResponse({
    required this.status,
    required this.accessToken,
    required this.refreshToken,
    required this.expiresIn,
  });

  factory OtpVerificationResponse.fromJson(
    Map<String, dynamic> json,
  ) {
    return OtpVerificationResponse(
      status:
          json['status']?.toString() ?? '',
      accessToken:
          json['accessToken']?.toString() ?? '',
      refreshToken:
          json['refreshToken']?.toString() ?? '',
      expiresIn:
          (json['expiresIn'] as num?)?.toInt() ?? 0,
    );
  }
}

// ================================================================
// REGISTRATION RESPONSE
// ================================================================

class RegisterResponse {
  final int id;
  final String userId;
  final String email;
  final String status;
  final bool totpEnabled;

  const RegisterResponse({
    required this.id,
    required this.userId,
    required this.email,
    required this.status,
    required this.totpEnabled,
  });

  factory RegisterResponse.fromJson(
    Map<String, dynamic> json,
  ) {
    return RegisterResponse(
      id:
          (json['id'] as num?)?.toInt() ?? 0,
      userId:
          json['userId']?.toString() ?? '',
      email:
          json['email']?.toString() ?? '',
      status:
          json['status']?.toString() ?? '',
      totpEnabled:
          json['totpEnabled'] as bool? ?? false,
    );
  }
}

// ================================================================
// EXISTING PASSWORD RESET RESPONSE
// ================================================================

class PasswordResetResponse {
  final String status;
  final String message;
  final String? resetToken;

  const PasswordResetResponse({
    required this.status,
    required this.message,
    required this.resetToken,
  });

  factory PasswordResetResponse.fromJson(
    Map<String, dynamic> json,
  ) {
    return PasswordResetResponse(
      status:
          json['status']?.toString() ?? '',
      message:
          json['message']?.toString() ?? '',
      resetToken:
          json['resetToken']?.toString(),
    );
  }
}

// ================================================================
// EMAIL RECOVERY VERIFICATION RESPONSE
// ================================================================

class PasswordRecoveryVerificationResponse {
  final String status;
  final String? resetToken;

  const PasswordRecoveryVerificationResponse({
    required this.status,
    required this.resetToken,
  });

  factory PasswordRecoveryVerificationResponse
      .fromJson(
    Map<String, dynamic> json,
  ) {
    return PasswordRecoveryVerificationResponse(
      status:
          json['status']?.toString() ?? '',
      resetToken:
          json['resetToken']?.toString(),
    );
  }
}
class LoginResponse {
  final String status;
  final String message;
  final bool totpRequired;
  final String? accessToken;
  final String? refreshToken;
  final String? tempAuthSessionId;
  final int? tempAuthSessionExpiresInSeconds;

  const LoginResponse({
    required this.status,
    required this.message,
    required this.totpRequired,
    this.accessToken,
    this.refreshToken,
    this.tempAuthSessionId,
    this.tempAuthSessionExpiresInSeconds,
  });

  factory LoginResponse.fromJson(
    Map<String, dynamic> json,
  ) {
    return LoginResponse(
      status: json['status'] as String,
      message: json['message'] as String,
      totpRequired:
          json['totpRequired'] as bool,
      accessToken:
          json['accessToken'] as String?,
      refreshToken:
          json['refreshToken'] as String?,
      tempAuthSessionId:
          (json['tempAuthSessionId'] ??
                  json['temporaryAuthSessionToken'])
              as String?,
      tempAuthSessionExpiresInSeconds:
          (json['tempAuthSessionExpiresInSeconds'] ??
                  json['temporaryAuthSessionExpiresInSeconds'])
              as int?,
    );
  }
}
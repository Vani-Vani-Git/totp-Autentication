class TotpEnrollmentResponse {
  final String secret;
  final String otpauthUri;
  final String issuer;
  final String accountName;
  final int digits;
  final int periodSeconds;
  final String algorithm;

  const TotpEnrollmentResponse({
    required this.secret,
    required this.otpauthUri,
    required this.issuer,
    required this.accountName,
    required this.digits,
    required this.periodSeconds,
    required this.algorithm,
  });

  factory TotpEnrollmentResponse.fromJson(
    Map<String, dynamic> json,
  ) {
    return TotpEnrollmentResponse(
      secret: json['secret'] as String,
      otpauthUri: json['otpauthUri'] as String,
      issuer: json['issuer'] as String,
      accountName: json['accountName'] as String,
      digits: json['digits'] as int,
      periodSeconds:
          json['periodSeconds'] as int,
      algorithm: json['algorithm'] as String,
    );
  }
}
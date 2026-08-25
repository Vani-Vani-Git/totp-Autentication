import 'package:flutter/material.dart';
import 'package:qr_flutter/qr_flutter.dart';

import '../models/totp_enrollment_response.dart';
import '../services/totp_service.dart';
import 'totp_confirmation_screen.dart';

class TotpEnrollmentScreen extends StatefulWidget {
  final String accessToken;

  const TotpEnrollmentScreen({
    super.key,
    required this.accessToken,
  });

  @override
  State<TotpEnrollmentScreen> createState() =>
      _TotpEnrollmentScreenState();
}

class _TotpEnrollmentScreenState
    extends State<TotpEnrollmentScreen> {
  final TotpService _totpService = TotpService();

  TotpEnrollmentResponse? _enrollment;

  bool _loading = true;
  String? _errorMessage;

  @override
  void initState() {
    super.initState();
    _loadEnrollment();
  }

  Future<void> _loadEnrollment() async {
    try {
      final enrollment =
          await _totpService.enroll(
        accessToken: widget.accessToken,
      );

      if (!mounted) {
        return;
      }

      setState(() {
        _enrollment = enrollment;
        _loading = false;
      });
    } catch (error) {
      if (!mounted) {
        return;
      }

      setState(() {
        _errorMessage = error
            .toString()
            .replaceFirst('Exception: ', '');
        _loading = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text(
          'Set Up Authenticator',
        ),
      ),
      body: SafeArea(
        child: _buildBody(),
      ),
    );
  }

  Widget _buildBody() {
    if (_loading) {
      return const Center(
        child: CircularProgressIndicator(),
      );
    }

    if (_errorMessage != null) {
      return Center(
        child: Padding(
          padding: const EdgeInsets.all(24),
          child: Column(
            mainAxisAlignment:
                MainAxisAlignment.center,
            children: [
              const Icon(
                Icons.error_outline,
                size: 54,
              ),
              const SizedBox(height: 16),
              Text(
                _errorMessage!,
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 20),
              FilledButton(
                onPressed: () {
                  setState(() {
                    _loading = true;
                    _errorMessage = null;
                  });

                  _loadEnrollment();
                },
                child: const Text('Try Again'),
              ),
            ],
          ),
        ),
      );
    }

    final enrollment = _enrollment;

    if (enrollment == null) {
      return const Center(
        child: Text(
          'Unable to load TOTP enrollment.',
        ),
      );
    }

    return Center(
      child: SingleChildScrollView(
        padding: const EdgeInsets.all(24),
        child: ConstrainedBox(
          constraints: const BoxConstraints(
            maxWidth: 500,
          ),
          child: Card(
            elevation: 4,
            child: Padding(
              padding: const EdgeInsets.all(28),
              child: Column(
                children: [
                  const Icon(
                    Icons.security_rounded,
                    size: 54,
                  ),

                  const SizedBox(height: 18),

                  const Text(
                    'Set Up Two-Factor Authentication',
                    textAlign: TextAlign.center,
                    style: TextStyle(
                      fontSize: 24,
                      fontWeight: FontWeight.bold,
                    ),
                  ),

                  const SizedBox(height: 12),

                  Text(
                    'Open Microsoft Authenticator and '
                    'scan this QR code.',
                    textAlign: TextAlign.center,
                    style: TextStyle(
                      color: Colors.grey.shade700,
                      fontSize: 15,
                    ),
                  ),

                  const SizedBox(height: 28),

                  Container(
                    padding: const EdgeInsets.all(18),
                    decoration: BoxDecoration(
                      color: Colors.white,
                      borderRadius:
                          BorderRadius.circular(18),
                      border: Border.all(
                        color: Colors.grey.shade300,
                      ),
                    ),
                    child: QrImageView(
                      data: enrollment.otpauthUri,
                      version: QrVersions.auto,
                      size: 240,
                    ),
                  ),

                  const SizedBox(height: 24),

                  Text(
                    enrollment.accountName,
                    textAlign: TextAlign.center,
                    style: const TextStyle(
                      fontWeight: FontWeight.w600,
                      fontSize: 16,
                    ),
                  ),

                  const SizedBox(height: 8),

                  Text(
                    '${enrollment.algorithm} • '
                    '${enrollment.digits} digits • '
                    '${enrollment.periodSeconds}s',
                    textAlign: TextAlign.center,
                    style: TextStyle(
                      color: Colors.grey.shade600,
                      fontSize: 13,
                    ),
                  ),

                  const SizedBox(height: 24),

                  const Text(
                    'Scan the QR code with Microsoft '
                    'Authenticator. It will generate a '
                    '6-digit verification code.',
                    textAlign: TextAlign.center,
                  ),

                  const SizedBox(height: 28),

SizedBox(
  width: double.infinity,
  height: 52,
  child: FilledButton(
    onPressed: () async {
      final bool? verified =
          await Navigator.of(context).push<bool>(
        MaterialPageRoute(
          builder: (_) => TotpConfirmationScreen(
            accessToken: widget.accessToken,
          ),
        ),
      );

      if (!mounted) {
        return;
      }

      if (verified == true) {
        Navigator.of(context).pop(true);
      }
    },
    child: const Text(
      'Continue',
      style: TextStyle(
        fontSize: 16,
        fontWeight: FontWeight.w600,
      ),
    ),
  ),
),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }
}
import 'package:flutter/material.dart';

import '../services/totp_confirmation_service.dart';

class TotpConfirmationScreen extends StatefulWidget {
  final String accessToken;

  const TotpConfirmationScreen({
    super.key,
    required this.accessToken,
  });

  @override
  State<TotpConfirmationScreen> createState() =>
      _TotpConfirmationScreenState();
}

class _TotpConfirmationScreenState
    extends State<TotpConfirmationScreen> {
  final TextEditingController _codeController =
      TextEditingController();

  final TotpConfirmationService _confirmationService =
      TotpConfirmationService();

  bool _isConfirming = false;

  @override
  void dispose() {
    _codeController.dispose();
    super.dispose();
  }

  Future<void> _confirmCode() async {
    final String code =
        _codeController.text.trim();

    if (!RegExp(r'^\d{6}$').hasMatch(code)) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text(
            'Enter the 6-digit code from Microsoft Authenticator.',
          ),
        ),
      );
      return;
    }

    setState(() {
      _isConfirming = true;
    });

    try {
      await _confirmationService.confirm(
        accessToken: widget.accessToken,
        code: code,
      );

      if (!mounted) {
        return;
      }

      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text(
            'Two-factor authentication enabled successfully.',
          ),
        ),
      );

      Navigator.of(context).pop(true);
    } catch (error) {
      if (!mounted) {
        return;
      }

      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(
            error.toString().replaceFirst(
                  'Exception: ',
                  '',
                ),
          ),
        ),
      );
    } finally {
      if (mounted) {
        setState(() {
          _isConfirming = false;
        });
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text(
          'Verify Authenticator',
        ),
      ),
      body: SafeArea(
        child: Center(
          child: SingleChildScrollView(
            padding: const EdgeInsets.all(24),
            child: ConstrainedBox(
              constraints: const BoxConstraints(
                maxWidth: 430,
              ),
              child: Card(
                child: Padding(
                  padding: const EdgeInsets.all(28),
                  child: Column(
                    children: [
                      const Icon(
                        Icons.password_rounded,
                        size: 56,
                      ),

                      const SizedBox(height: 20),

                      const Text(
                        'Enter Verification Code',
                        textAlign: TextAlign.center,
                        style: TextStyle(
                          fontSize: 24,
                          fontWeight: FontWeight.bold,
                        ),
                      ),

                      const SizedBox(height: 12),

                      Text(
                        'Open Microsoft Authenticator and '
                        'enter the current 6-digit code.',
                        textAlign: TextAlign.center,
                        style: TextStyle(
                          color: Colors.grey.shade700,
                        ),
                      ),

                      const SizedBox(height: 28),

                      TextField(
                        controller: _codeController,
                        keyboardType:
                            TextInputType.number,
                        maxLength: 6,
                        textAlign: TextAlign.center,
                        style: const TextStyle(
                          fontSize: 28,
                          fontWeight: FontWeight.w600,
                          letterSpacing: 8,
                        ),
                        decoration:
                            const InputDecoration(
                          labelText: '6-digit code',
                          counterText: '',
                          border: OutlineInputBorder(),
                        ),
                      ),

                      const SizedBox(height: 24),

                      SizedBox(
                        width: double.infinity,
                        height: 52,
                        child: FilledButton(
                          onPressed: _isConfirming
                              ? null
                              : _confirmCode,
                          child: _isConfirming
                              ? const SizedBox(
                                  width: 22,
                                  height: 22,
                                  child:
                                      CircularProgressIndicator(
                                    strokeWidth: 2,
                                    color: Colors.white,
                                  ),
                                )
                              : const Text(
                                  'Verify Code',
                                  style: TextStyle(
                                    fontSize: 16,
                                    fontWeight:
                                        FontWeight.w600,
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
        ),
      ),
    );
  }
}
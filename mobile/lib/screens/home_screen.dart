import 'package:flutter/material.dart';

import '../core/secure_storage_service.dart';
import 'totp_enrollment_screen.dart';
import 'login_screen.dart';

class HomeScreen extends StatefulWidget {
  final String accessToken;

  const HomeScreen({
    super.key,
    required this.accessToken,
  });

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  bool _openingTotpSetup = false;

  Future<void> _openTotpSetup() async {
    setState(() {
      _openingTotpSetup = true;
    });

    try {
      final bool? completed =
          await Navigator.of(context).push<bool>(
        MaterialPageRoute(
          builder: (_) => TotpEnrollmentScreen(
            accessToken: widget.accessToken,
          ),
        ),
      );

      if (!mounted) {
        return;
      }

      if (completed == true) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text(
              'Two-factor authentication is now enabled.',
            ),
          ),
        );
      }
    } finally {
      if (mounted) {
        setState(() {
          _openingTotpSetup = false;
        });
      }
    }
  }
  Future<void> _logout() async {
  final SecureStorageService storage =
      SecureStorageService();

  await storage.clearTokens();

  if (!mounted) {
    return;
  }

  Navigator.of(context).pushAndRemoveUntil(
    MaterialPageRoute(
      builder: (_) => const LoginScreen(),
    ),
    (route) => false,
  );
}
 
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('TOTP Authentication'),
        actions: [
          IconButton(
            tooltip: 'Logout',
            onPressed: _logout,
            icon: const Icon(Icons.logout),
          ),
        ],
      ),
      body: SafeArea(
        child: Center(
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
                        Icons.verified_user_rounded,
                        size: 64,
                      ),

                      const SizedBox(height: 20),

                      const Text(
                        'Welcome',
                        textAlign: TextAlign.center,
                        style: TextStyle(
                          fontSize: 28,
                          fontWeight: FontWeight.bold,
                        ),
                      ),

                      const SizedBox(height: 12),

                      Text(
                        'You have successfully signed in.',
                        textAlign: TextAlign.center,
                        style: TextStyle(
                          color: Colors.grey.shade700,
                          fontSize: 15,
                        ),
                      ),

                      const SizedBox(height: 32),

                      SizedBox(
                        width: double.infinity,
                        height: 54,
                        child: FilledButton.icon(
                          onPressed: _openingTotpSetup
                              ? null
                              : _openTotpSetup,
                          icon: _openingTotpSetup
                              ? const SizedBox(
                                  width: 20,
                                  height: 20,
                                  child:
                                      CircularProgressIndicator(
                                    strokeWidth: 2,
                                    color: Colors.white,
                                  ),
                                )
                              : const Icon(
                                  Icons.security,
                                ),
                          label: const Text(
                            'Set Up Two Factor Authentication',
                            style: TextStyle(
                              fontSize: 15,
                              fontWeight: FontWeight.w600,
                            ),
                          ),
                        ),
                      ),

                      const SizedBox(height: 16),

                      Text(
                        'Protect your account with a '
                        '6-digit authenticator code.',
                        textAlign: TextAlign.center,
                        style: TextStyle(
                          color: Colors.grey.shade600,
                          fontSize: 13,
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
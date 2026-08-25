import 'package:flutter/material.dart';
import '../services/auth_service.dart';
import '../core/secure_storage_service.dart';
import 'home_screen.dart';
import 'otp_verification_screen.dart';
import 'register_screen.dart';
import 'forgot_password_screen.dart';

class LoginScreen extends StatefulWidget {
  const LoginScreen({super.key});

  @override
  State<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen>
    with SingleTickerProviderStateMixin {
  final TextEditingController _identifierController =
      TextEditingController();

  final TextEditingController _passwordController =
      TextEditingController();
  final AuthService _authService = AuthService();
  bool _isLoggingIn = false;

  late final AnimationController _backgroundController;

  bool _obscurePassword = true;

  @override
  void initState() {
    super.initState();

    _backgroundController = AnimationController(
      vsync: this,
      duration: const Duration(seconds: 10),
    )..repeat();
  }

  @override
  void dispose() {
    _backgroundController.dispose();
    _identifierController.dispose();
    _passwordController.dispose();
    super.dispose();
  }
  Future<void> _handleLogin() async {
  final String identifier =
      _identifierController.text.trim();

  final String password =
      _passwordController.text;

  if (identifier.isEmpty || password.isEmpty) {
    if (!mounted) {
      return;
    }

    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(
        content: Text(
          'Please enter your identifier and password.',
        ),
      ),
    );

    return;
  }

  setState(() {
    _isLoggingIn = true;
  });

  try {
    final response = await _authService.login(
      identifier: identifier,
      password: password,
    );

    if (!mounted) {
      return;
    }

    if (response.totpRequired) {
      final String? tempAuthSessionId =
          response.tempAuthSessionId;

      if (tempAuthSessionId == null ||
          tempAuthSessionId.isEmpty) {
        throw Exception(
          'Temporary authentication session was not returned.',
        );
      }

      final int expiresIn =
          response.tempAuthSessionExpiresInSeconds ?? 300;

      final bool? verified =
          await Navigator.of(context).push<bool>(
        MaterialPageRoute(
          builder: (_) => OtpVerificationScreen(
            tempAuthSessionId: tempAuthSessionId,
            expiresInSeconds: expiresIn,
          ),
        ),
      );

      if (!mounted) {
        return;
      }

      if (verified == true) {
        return;
      }

      return;
    }

    final String? accessToken =
        response.accessToken;

    final String? refreshToken =
        response.refreshToken;

    if (accessToken == null ||
        refreshToken == null) {
      throw Exception(
        'Authentication tokens were not returned.',
      );
    }

    final SecureStorageService storage =
        SecureStorageService();

    await storage.saveTokens(
      accessToken: accessToken,
      refreshToken: refreshToken,
    );

    if (!mounted) {
      return;
    }

    await Navigator.of(context).pushReplacement(
      MaterialPageRoute(
        builder: (_) => HomeScreen(
          accessToken: accessToken,
        ),
      ),
    );
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
        _isLoggingIn = false;
      });
    }
  }
}
 
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Stack(
        children: [
          _buildAnimatedBackground(),

          SafeArea(
            child: Center(
              child: SingleChildScrollView(
                padding: const EdgeInsets.symmetric(
                  horizontal: 24,
                  vertical: 32,
                ),
                child: ConstrainedBox(
                  constraints: const BoxConstraints(
                    maxWidth: 430,
                  ),
                  child: _buildLoginCard(),
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildAnimatedBackground() {
    return AnimatedBuilder(
      animation: _backgroundController,
      builder: (context, child) {
        final double progress =
            _backgroundController.value;

        return Container(
          decoration: const BoxDecoration(
            gradient: LinearGradient(
              begin: Alignment.topLeft,
              end: Alignment.bottomRight,
              colors: [
                Color(0xFF080B1A),
                Color(0xFF101A3A),
                Color(0xFF171033),
              ],
            ),
          ),
          child: Stack(
            children: [
              Positioned(
                left: -80 + (progress * 120),
                top: 80,
                child: _glowCircle(
                  size: 260,
                  color: const Color(0xFF536DFE),
                ),
              ),
              Positioned(
                right: -100 + (progress * 140),
                top: 220,
                child: _glowCircle(
                  size: 300,
                  color: const Color(0xFF9C27B0),
                ),
              ),
              Positioned(
                left: 80 - (progress * 100),
                bottom: -130,
                child: _glowCircle(
                  size: 320,
                  color: const Color(0xFF00BCD4),
                ),
              ),
              Positioned.fill(
                child: CustomPaint(
                  painter: _SecurityBackgroundPainter(
                    progress: progress,
                  ),
                ),
              ),
            ],
          ),
        );
      },
    );
  }

  Widget _glowCircle({
    required double size,
    required Color color,
  }) {
    return Container(
      width: size,
      height: size,
      decoration: BoxDecoration(
        shape: BoxShape.circle,
        gradient: RadialGradient(
          colors: [
            color.withValues(alpha: 0.35),
            color.withValues(alpha: 0.08),
            color.withValues(alpha: 0.0),
          ],
        ),
      ),
    );
  }

  Widget _buildLoginCard() {
    return Container(
      decoration: BoxDecoration(
        color: Colors.white.withValues(alpha: 0.94),
        borderRadius: BorderRadius.circular(28),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.30),
            blurRadius: 35,
            spreadRadius: 3,
            offset: const Offset(0, 15),
          ),
        ],
      ),
      child: Padding(
        padding: const EdgeInsets.all(30),
        child: Column(
          crossAxisAlignment:
              CrossAxisAlignment.stretch,
          children: [
            Container(
              width: 72,
              height: 72,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                gradient: const LinearGradient(
                  colors: [
                    Color(0xFF536DFE),
                    Color(0xFF7C4DFF),
                  ],
                ),
                boxShadow: [
                  BoxShadow(
                    color: const Color(0xFF536DFE)
                        .withValues(alpha: 0.35),
                    blurRadius: 20,
                    spreadRadius: 2,
                  ),
                ],
              ),
              child: const Icon(
                Icons.shield_rounded,
                size: 38,
                color: Colors.white,
              ),
            ),

            const SizedBox(height: 22),

            const Text(
              'Welcome Back',
              textAlign: TextAlign.center,
              style: TextStyle(
                fontSize: 28,
                fontWeight: FontWeight.w700,
                color: Color(0xFF171A2B),
              ),
            ),

            const SizedBox(height: 8),

            Text(
              'Secure access to your account',
              textAlign: TextAlign.center,
              style: TextStyle(
                fontSize: 14,
                color: Colors.grey.shade600,
              ),
            ),

            const SizedBox(height: 30),

            TextField(
              controller: _identifierController,
              keyboardType: TextInputType.emailAddress,
              textInputAction: TextInputAction.next,
              decoration: InputDecoration(
                labelText: 'User ID or Email',
                hintText: 'Enter your user ID or email',
                prefixIcon: const Icon(
                  Icons.person_outline,
                ),
                filled: true,
                fillColor: const Color(0xFFF5F6FA),
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(14),
                  borderSide: BorderSide.none,
                ),
              ),
            ),

            const SizedBox(height: 16),

            TextField(
              controller: _passwordController,
              obscureText: _obscurePassword,
              textInputAction: TextInputAction.done,
              onSubmitted: (_) => _handleLogin(),
              decoration: InputDecoration(
                labelText: 'Password',
                hintText: 'Enter your password',
                prefixIcon: const Icon(
                  Icons.lock_outline,
                ),
                suffixIcon: IconButton(
                  onPressed: () {
                    setState(() {
                      _obscurePassword =
                          !_obscurePassword;
                    });
                  },
                  icon: Icon(
                    _obscurePassword
                        ? Icons.visibility_outlined
                        : Icons.visibility_off_outlined,
                  ),
                ),
                filled: true,
                fillColor: const Color(0xFFF5F6FA),
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(14),
                  borderSide: BorderSide.none,
                ),
              ),
            ),

            const SizedBox(height: 12),

            Align(
              alignment: Alignment.centerRight,
              child: TextButton(
                onPressed: () {
  Navigator.of(context).push(
    MaterialPageRoute(
      builder: (_) => const ForgotPasswordScreen(),
    ),
  );
},
                child: const Text(
                  'Forgot Password?',
                ),
              ),
            ),

            const SizedBox(height: 8),

            SizedBox(
              height: 54,
              child: FilledButton(
                onPressed: _handleLogin,
                style: FilledButton.styleFrom(
                  shape: RoundedRectangleBorder(
                    borderRadius:
                        BorderRadius.circular(14),
                  ),
                ),
                child: _isLoggingIn
    ? const SizedBox(
        width: 22,
        height: 22,
        child: CircularProgressIndicator(
          strokeWidth: 2,
          color: Colors.white,
        ),
      )
    : const Text(
        'Sign In',
        style: TextStyle(
          fontSize: 16,
          fontWeight: FontWeight.w600,
        ),
      ),
              ),
            ),
            const SizedBox(height: 18),

Row(
  mainAxisAlignment: MainAxisAlignment.center,
  children: [
    Text(
      "Don't have an account?",
      style: const TextStyle(
      color: Color.fromARGB(255, 8, 8, 8),
      fontSize: 13,
      ),
    ),
    TextButton(
      onPressed: () {
        Navigator.of(context).push(
          MaterialPageRoute(
            builder: (_) => const RegisterScreen(),
          ),
        );
      },
      child: const Text(
        'Create Account',
        style: TextStyle(
          color: Color(0xFF73BBFF),
          fontSize: 13,
          fontWeight: FontWeight.w700,
        ),
      ),
    ),
  ],
),

            const SizedBox(height: 22),

            Row(
              mainAxisAlignment:
                  MainAxisAlignment.center,
              children: [
                Icon(
                  Icons.verified_user_outlined,
                  size: 16,
                  color: Colors.grey.shade600,
                ),
                const SizedBox(width: 6),
                Text(
                  'Protected with TOTP security',
                  style: TextStyle(
                    fontSize: 12,
                    color: Colors.grey.shade600,
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

class _SecurityBackgroundPainter
    extends CustomPainter {
  final double progress;

  _SecurityBackgroundPainter({
    required this.progress,
  });

  @override
  void paint(
    Canvas canvas,
    Size size,
  ) {
    final Paint paint = Paint()
      ..style = PaintingStyle.stroke
      ..strokeWidth = 1;

    for (int i = 0; i < 8; i++) {
      final double radius =
          90.0 + (i * 45.0) + (progress * 30);

      final Offset center = Offset(
        size.width *
            (0.15 + (i % 3) * 0.35),
        size.height *
            (0.15 + ((i + 1) % 4) * 0.25),
      );

      paint.color = Colors.white.withValues(
        alpha: 0.025,
      );

      canvas.drawCircle(
        center,
        radius,
        paint,
      );
    }
  }

  @override
  bool shouldRepaint(
    covariant _SecurityBackgroundPainter oldDelegate,
  ) {
    return oldDelegate.progress != progress;
  }
}
import 'dart:ui';

import 'package:flutter/material.dart';

import '../services/auth_service.dart';
import 'new_password_screen.dart';

class PasswordResetOtpScreen extends StatefulWidget {
  final String resetToken;

  const PasswordResetOtpScreen({
    super.key,
    required this.resetToken,
  });

  @override
  State<PasswordResetOtpScreen> createState() =>
      _PasswordResetOtpScreenState();
}

class _PasswordResetOtpScreenState
    extends State<PasswordResetOtpScreen>
    with TickerProviderStateMixin {
  late final AnimationController _backgroundController;
  late final AnimationController _contentController;

  final _otpController = TextEditingController();
  final _otpFocusNode = FocusNode();

  final AuthService _authService = AuthService();

  bool _isLoading = false;

  @override
  void initState() {
    super.initState();

    _backgroundController = AnimationController(
      vsync: this,
      duration: const Duration(seconds: 14),
    )..repeat();

    _contentController = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 800),
    )..forward();
  }

  @override
  void dispose() {
    _backgroundController.dispose();
    _contentController.dispose();
    _otpController.dispose();
    _otpFocusNode.dispose();

    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final size = MediaQuery.sizeOf(context);
    final bottomInset = MediaQuery.viewInsetsOf(context).bottom;

    return Scaffold(
      backgroundColor: const Color(0xFF060A18),
      resizeToAvoidBottomInset: true,
      body: Stack(
        children: [
          Positioned.fill(
            child: _buildAnimatedBackground(size),
          ),
          SafeArea(
            child: SingleChildScrollView(
              physics: const BouncingScrollPhysics(),
              padding: EdgeInsets.fromLTRB(
                20,
                12,
                20,
                28 + bottomInset,
              ),
              child: FadeTransition(
                opacity: CurvedAnimation(
                  parent: _contentController,
                  curve: Curves.easeOut,
                ),
                child: SlideTransition(
                  position: Tween<Offset>(
                    begin: const Offset(0, 0.06),
                    end: Offset.zero,
                  ).animate(
                    CurvedAnimation(
                      parent: _contentController,
                      curve: Curves.easeOutCubic,
                    ),
                  ),
                  child: _buildCard(),
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildAnimatedBackground(Size size) {
    return AnimatedBuilder(
      animation: _backgroundController,
      builder: (context, child) {
        final progress = _backgroundController.value;

        return Stack(
          children: [
            Container(
              decoration: const BoxDecoration(
                gradient: LinearGradient(
                  begin: Alignment.topLeft,
                  end: Alignment.bottomRight,
                  colors: [
                    Color(0xFF071326),
                    Color(0xFF0A1730),
                    Color(0xFF050816),
                  ],
                ),
              ),
            ),
            _buildGlowOrb(
              size: size.width * 0.90,
              left: -size.width * 0.35 +
                  progress * size.width * 0.12,
              top: -size.width * 0.28,
              opacity: 0.16,
            ),
            _buildGlowOrb(
              size: size.width * 0.70,
              right: -size.width * 0.28 -
                  progress * size.width * 0.10,
              bottom: size.height * 0.18,
              opacity: 0.11,
            ),
            CustomPaint(
              size: Size.infinite,
              painter: _PasswordResetOtpGridPainter(
                progress: progress,
              ),
            ),
          ],
        );
      },
    );
  }

  Widget _buildGlowOrb({
    required double size,
    double? left,
    double? right,
    double? top,
    double? bottom,
    required double opacity,
  }) {
    return Positioned(
      left: left,
      right: right,
      top: top,
      bottom: bottom,
      child: IgnorePointer(
        child: Container(
          width: size,
          height: size,
          decoration: BoxDecoration(
            shape: BoxShape.circle,
            gradient: RadialGradient(
              colors: [
                const Color(0xFF318BFF).withValues(
                  alpha: opacity,
                ),
                Colors.transparent,
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildCard() {
    return ClipRRect(
      borderRadius: BorderRadius.circular(30),
      child: BackdropFilter(
        filter: ImageFilter.blur(
          sigmaX: 18,
          sigmaY: 18,
        ),
        child: Container(
          width: double.infinity,
          constraints: const BoxConstraints(
            maxWidth: 440,
          ),
          padding: const EdgeInsets.fromLTRB(
            22,
            28,
            22,
            24,
          ),
          decoration: BoxDecoration(
            color: const Color(0xFF101A2C).withValues(
              alpha: 0.86,
            ),
            borderRadius: BorderRadius.circular(30),
            border: Border.all(
              color: Colors.white.withValues(
                alpha: 0.10,
              ),
            ),
            boxShadow: [
              BoxShadow(
                color: Colors.black.withValues(
                  alpha: 0.30,
                ),
                blurRadius: 40,
                offset: const Offset(0, 18),
              ),
            ],
          ),
          child: Column(
            children: [
              _buildHeader(),

              const SizedBox(height: 28),

              _buildOtpField(),

              const SizedBox(height: 24),

              _buildVerifyButton(),

              const SizedBox(height: 18),

              TextButton.icon(
                onPressed: _isLoading
                    ? null
                    : () {
                        Navigator.of(context).pop();
                      },
                icon: const Icon(
                  Icons.arrow_back_rounded,
                  size: 17,
                ),
                label: const Text(
                  'Back',
                ),
                style: TextButton.styleFrom(
                  foregroundColor: const Color(
                    0xFF73BBFF,
                  ),
                ),
              ),

              const SizedBox(height: 8),

              _buildSecurityNote(),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildHeader() {
    return Column(
      children: [
        Container(
          width: 72,
          height: 72,
          decoration: BoxDecoration(
            shape: BoxShape.circle,
            color: const Color(0xFF1677FF).withValues(
              alpha: 0.12,
            ),
            border: Border.all(
              color: const Color(0xFF69B4FF).withValues(
                alpha: 0.25,
              ),
            ),
            boxShadow: [
              BoxShadow(
                color: const Color(0xFF1677FF).withValues(
                  alpha: 0.14,
                ),
                blurRadius: 28,
                spreadRadius: 4,
              ),
            ],
          ),
          child: const Icon(
            Icons.verified_user_outlined,
            color: Color(0xFF73BBFF),
            size: 34,
          ),
        ),

        const SizedBox(height: 18),

        const Text(
          'Verify Identity',
          textAlign: TextAlign.center,
          style: TextStyle(
            color: Colors.white,
            fontSize: 27,
            fontWeight: FontWeight.w700,
            letterSpacing: -0.5,
          ),
        ),

        const SizedBox(height: 8),

        Text(
          'Enter the 6-digit code from your authenticator',
          textAlign: TextAlign.center,
          style: TextStyle(
            color: Colors.white.withValues(
              alpha: 0.62,
            ),
            fontSize: 14,
          ),
        ),
      ],
    );
  }

  Widget _buildOtpField() {
    return TextField(
      controller: _otpController,
      focusNode: _otpFocusNode,
      keyboardType: TextInputType.number,
      textInputAction: TextInputAction.done,
      maxLength: 6,
      textAlign: TextAlign.center,
      onSubmitted: (_) {
        _verifyOtp();
      },
      style: const TextStyle(
        color: Colors.white,
        fontSize: 25,
        fontWeight: FontWeight.w700,
        letterSpacing: 7,
      ),
      decoration: InputDecoration(
        counterText: '',
        hintText: '000000',
        hintStyle: TextStyle(
          color: Colors.white.withValues(
            alpha: 0.18,
          ),
          fontSize: 25,
          fontWeight: FontWeight.w700,
          letterSpacing: 7,
        ),
        prefixIcon: Icon(
          Icons.lock_clock_outlined,
          color: Colors.white.withValues(
            alpha: 0.50,
          ),
        ),
        filled: true,
        fillColor: Colors.white.withValues(
          alpha: 0.055,
        ),
        contentPadding: const EdgeInsets.symmetric(
          vertical: 17,
        ),
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(16),
          borderSide: BorderSide(
            color: Colors.white.withValues(
              alpha: 0.07,
            ),
          ),
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(16),
          borderSide: BorderSide(
            color: Colors.white.withValues(
              alpha: 0.07,
            ),
          ),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(16),
          borderSide: const BorderSide(
            color: Color(0xFF4DA3FF),
            width: 1.3,
          ),
        ),
      ),
    );
  }

  Widget _buildVerifyButton() {
    return SizedBox(
      width: double.infinity,
      height: 55,
      child: ElevatedButton(
        onPressed: _isLoading ? null : _verifyOtp,
        style: ElevatedButton.styleFrom(
          elevation: 0,
          backgroundColor: const Color(0xFF5868AD),
          disabledBackgroundColor:
              const Color(0xFF5868AD).withValues(
            alpha: 0.55,
          ),
          foregroundColor: Colors.white,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(16),
          ),
        ),
        child: _isLoading
            ? const SizedBox(
                width: 22,
                height: 22,
                child: CircularProgressIndicator(
                  strokeWidth: 2.4,
                  valueColor:
                      AlwaysStoppedAnimation<Color>(
                    Colors.white,
                  ),
                ),
              )
            : const Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Icon(
                    Icons.verified_rounded,
                    size: 19,
                  ),
                  SizedBox(width: 9),
                  Text(
                    'Verify Code',
                    style: TextStyle(
                      fontSize: 15,
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                ],
              ),
      ),
    );
  }

  Widget _buildSecurityNote() {
    return Row(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        Icon(
          Icons.shield_outlined,
          size: 15,
          color: Colors.white.withValues(
            alpha: 0.35,
          ),
        ),
        const SizedBox(width: 7),
        Text(
          'Protected by TOTP verification',
          style: TextStyle(
            color: Colors.white.withValues(
              alpha: 0.38,
            ),
            fontSize: 11.5,
          ),
        ),
      ],
    );
  }

  Future<void> _verifyOtp() async {
    final code = _otpController.text.trim();

    if (!RegExp(r'^\d{6}$').hasMatch(code)) {
      _showMessage(
        'Enter the 6-digit authenticator code.',
      );
      return;
    }

    setState(() {
      _isLoading = true;
    });

    try {
      await _authService.verifyPasswordReset(
        resetToken: widget.resetToken,
        totpCode: code,
      );

      if (!mounted) {
        return;
      }

      Navigator.of(context).push(
        MaterialPageRoute(
          builder: (_) => NewPasswordScreen(
            resetToken: widget.resetToken,
          ),
        ),
      );
    } catch (error) {
      if (!mounted) {
        return;
      }

      _showMessage(
        error.toString().replaceFirst(
              'Exception: ',
              '',
            ),
      );
    } finally {
      if (mounted) {
        setState(() {
          _isLoading = false;
        });
      }
    }
  }

  void _showMessage(String message) {
    if (!mounted) {
      return;
    }

    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message),
      ),
    );
  }
}

class _PasswordResetOtpGridPainter
    extends CustomPainter {
  final double progress;

  const _PasswordResetOtpGridPainter({
    required this.progress,
  });

  @override
  void paint(
    Canvas canvas,
    Size size,
  ) {
    final paint = Paint()
      ..style = PaintingStyle.stroke
      ..strokeWidth = 0.7
      ..color = Colors.white.withValues(
        alpha: 0.022,
      );

    const spacing = 48.0;
    final shift = progress * spacing;

    for (
      double x = -spacing + shift;
      x < size.width + spacing;
      x += spacing
    ) {
      canvas.drawLine(
        Offset(x, 0),
        Offset(x, size.height),
        paint,
      );
    }

    for (
      double y = -spacing + shift;
      y < size.height + spacing;
      y += spacing
    ) {
      canvas.drawLine(
        Offset(0, y),
        Offset(size.width, y),
        paint,
      );
    }

    final nodePaint = Paint()
      ..style = PaintingStyle.fill
      ..color = const Color(0xFF5AAFFF).withValues(
        alpha: 0.05,
      );

    for (
      double x = spacing;
      x < size.width;
      x += spacing * 2
    ) {
      for (
        double y = spacing;
        y < size.height;
        y += spacing * 2
      ) {
        canvas.drawCircle(
          Offset(x, y),
          1.4,
          nodePaint,
        );
      }
    }
  }

  @override
  bool shouldRepaint(
    covariant _PasswordResetOtpGridPainter
        oldDelegate,
  ) {
    return oldDelegate.progress != progress;
  }
}
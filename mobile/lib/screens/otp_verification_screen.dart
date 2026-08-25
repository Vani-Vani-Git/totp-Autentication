import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../core/secure_storage_service.dart';
import '../services/auth_service.dart';
import 'welcome_screen.dart';

class OtpVerificationScreen extends StatefulWidget {
  final String tempAuthSessionId;
  final int expiresInSeconds;

  const OtpVerificationScreen({
    super.key,
    required this.tempAuthSessionId,
    required this.expiresInSeconds,
  });

  @override
  State<OtpVerificationScreen> createState() =>
      _OtpVerificationScreenState();
}

class _OtpVerificationScreenState extends State<OtpVerificationScreen>
    with TickerProviderStateMixin {
  late final AnimationController _backgroundController;
  late final AnimationController _pulseController;

  final TextEditingController _otpController =
      TextEditingController();

  final FocusNode _otpFocusNode = FocusNode();
  final AuthService _authService = AuthService();

  final SecureStorageService _storage =
    SecureStorageService();

  late int _remainingSeconds;

  bool _isVerifying = false;

  @override
  void initState() {
    super.initState();

    _remainingSeconds = widget.expiresInSeconds;

    _backgroundController = AnimationController(
      vsync: this,
      duration: const Duration(seconds: 12),
    )..repeat();

    _pulseController = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 1600),
    )..repeat(reverse: true);

    _startCountdown();
  }

  void _startCountdown() {
    Future.doWhile(() async {
      await Future<void>.delayed(
        const Duration(seconds: 1),
      );

      if (!mounted) {
        return false;
      }

      if (_remainingSeconds <= 0) {
        setState(() {});
        return false;
      }

      setState(() {
        _remainingSeconds--;
      });

      return _remainingSeconds > 0;
    });
  }

  @override
  void dispose() {
    _backgroundController.dispose();
    _pulseController.dispose();
    _otpController.dispose();
    _otpFocusNode.dispose();

    super.dispose();
  }

  String get _formattedTime {
    final minutes = _remainingSeconds ~/ 60;
    final seconds = _remainingSeconds % 60;

    return '${minutes.toString().padLeft(2, '0')}:'
        '${seconds.toString().padLeft(2, '0')}';
  }

  double get _progress {
    if (widget.expiresInSeconds <= 0) {
      return 0;
    }

    return (_remainingSeconds / widget.expiresInSeconds)
        .clamp(0.0, 1.0);
  }

  @override
  Widget build(BuildContext context) {
    final size = MediaQuery.sizeOf(context);
    final bottomInset = MediaQuery.viewInsetsOf(context).bottom;

    return Scaffold(
      backgroundColor: const Color(0xFF050816),
      resizeToAvoidBottomInset: true,
      body: Stack(
        children: [
          Positioned.fill(
            child: _buildAnimatedBackground(size),
          ),

          Positioned.fill(
            child: SafeArea(
              child: SingleChildScrollView(
                physics: const BouncingScrollPhysics(),
                padding: EdgeInsets.fromLTRB(
                  22,
                  24,
                  22,
                  24 + bottomInset,
                ),
                child: ConstrainedBox(
                  constraints: BoxConstraints(
                    minHeight: size.height -
                        MediaQuery.paddingOf(context).vertical -
                        48,
                  ),
                  child: Center(
                    child: _buildVerificationCard(),
                  ),
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
                    Color(0xFF07111F),
                    Color(0xFF08152B),
                    Color(0xFF050816),
                  ],
                ),
              ),
            ),

            _glowOrb(
              size: size.width * 0.85,
              left: -size.width * 0.35 +
                  (size.width * 0.08 * progress),
              top: size.height * 0.02,
              opacity: 0.16,
            ),

            _glowOrb(
              size: size.width * 0.65,
              right: -size.width * 0.25 -
                  (size.width * 0.10 * progress),
              top: size.height * 0.42,
              opacity: 0.12,
            ),

            _glowOrb(
              size: size.width * 0.55,
              left: size.width * 0.18,
              bottom: -size.width * 0.30,
              opacity: 0.09,
            ),

            CustomPaint(
              size: Size.infinite,
              painter: _SecurityGridPainter(
                progress: progress,
              ),
            ),
          ],
        );
      },
    );
  }

  Widget _glowOrb({
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
                const Color(0xFF2D8CFF).withValues(
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

  Widget _buildVerificationCard() {
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
              alpha: 0.82,
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
            mainAxisSize: MainAxisSize.min,
            children: [
              _buildSecurityIcon(),

              const SizedBox(height: 22),

              const Text(
                'Verify Your Identity',
                textAlign: TextAlign.center,
                style: TextStyle(
                  color: Colors.white,
                  fontSize: 27,
                  fontWeight: FontWeight.w700,
                  letterSpacing: -0.5,
                ),
              ),

              const SizedBox(height: 10),

              Text(
                'Enter the 6-digit code from your authenticator app.',
                textAlign: TextAlign.center,
                style: TextStyle(
                  color: Colors.white.withValues(
                    alpha: 0.68,
                  ),
                  fontSize: 14.5,
                  height: 1.5,
                ),
              ),

              const SizedBox(height: 28),

              _buildOtpInput(),

              const SizedBox(height: 20),

              _buildCountdown(),

              const SizedBox(height: 26),

              _buildVerifyButton(),

              const SizedBox(height: 18),

              Text(
                'Your code changes automatically every 30 seconds.',
                textAlign: TextAlign.center,
                style: TextStyle(
                  color: Colors.white.withValues(
                    alpha: 0.42,
                  ),
                  fontSize: 12,
                  height: 1.4,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildSecurityIcon() {
    return AnimatedBuilder(
      animation: _pulseController,
      builder: (context, child) {
        final scale =
            1.0 + (_pulseController.value * 0.04);

        return Transform.scale(
          scale: scale,
          child: Container(
            width: 78,
            height: 78,
            decoration: BoxDecoration(
              shape: BoxShape.circle,
              color: const Color(0xFF1677FF).withValues(
                alpha: 0.12,
              ),
              border: Border.all(
                color: const Color(0xFF4DA3FF).withValues(
                  alpha: 0.28,
                ),
              ),
              boxShadow: [
                BoxShadow(
                  color: const Color(0xFF1677FF).withValues(
                    alpha: 0.16,
                  ),
                  blurRadius: 28,
                  spreadRadius: 4,
                ),
              ],
            ),
            child: const Icon(
              Icons.verified_user_rounded,
              size: 37,
              color: Color(0xFF69B4FF),
            ),
          ),
        );
      },
    );
  }

  Widget _buildOtpInput() {
    return TextField(
      controller: _otpController,
      focusNode: _otpFocusNode,
      autofocus: true,
      keyboardType: TextInputType.number,
      textInputAction: TextInputAction.done,
      textAlign: TextAlign.center,
      maxLength: 6,
      style: const TextStyle(
        color: Colors.white,
        fontSize: 27,
        fontWeight: FontWeight.w700,
        letterSpacing: 9,
      ),
      inputFormatters: [
        FilteringTextInputFormatter.digitsOnly,
      ],
      decoration: InputDecoration(
        counterText: '',
        hintText: '••••••',
        hintStyle: TextStyle(
          color: Colors.white.withValues(
            alpha: 0.18,
          ),
          fontSize: 27,
          letterSpacing: 9,
        ),
        filled: true,
        fillColor: Colors.white.withValues(
          alpha: 0.055,
        ),
        contentPadding: const EdgeInsets.symmetric(
          horizontal: 18,
          vertical: 18,
        ),
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(18),
          borderSide: BorderSide(
            color: Colors.white.withValues(
              alpha: 0.08,
            ),
          ),
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(18),
          borderSide: BorderSide(
            color: Colors.white.withValues(
              alpha: 0.08,
            ),
          ),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(18),
          borderSide: const BorderSide(
            color: Color(0xFF4DA3FF),
            width: 1.4,
          ),
        ),
      ),
      onChanged: (_) {
  setState(() {});
},
onSubmitted: (_) {
  if (_otpController.text.length == 6) {
    _verifyOtp();
  }
},
    );
  }

  Widget _buildCountdown() {
    final expired = _remainingSeconds <= 0;

    return Column(
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              expired
                  ? Icons.timer_off_rounded
                  : Icons.timer_outlined,
              size: 17,
              color: expired
                  ? const Color(0xFFFF7A7A)
                  : const Color(0xFF72B7FF),
            ),
            const SizedBox(width: 7),
            Text(
              expired
                  ? 'Verification session expired'
                  : 'Session expires in $_formattedTime',
              style: TextStyle(
                color: expired
                    ? const Color(0xFFFF9B9B)
                    : Colors.white.withValues(
                        alpha: 0.68,
                      ),
                fontSize: 13,
                fontWeight: FontWeight.w500,
              ),
            ),
          ],
        ),

        const SizedBox(height: 10),

        ClipRRect(
          borderRadius: BorderRadius.circular(20),
          child: LinearProgressIndicator(
            value: _progress,
            minHeight: 4,
            backgroundColor: Colors.white.withValues(
              alpha: 0.06,
            ),
            valueColor: AlwaysStoppedAnimation<Color>(
              expired
                  ? const Color(0xFFFF6B6B)
                  : const Color(0xFF4DA3FF),
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildVerifyButton() {
    final canVerify =
        _otpController.text.length == 6 &&
        !_isVerifying &&
        _remainingSeconds > 0;

    return SizedBox(
      width: double.infinity,
      height: 56,
      child: ElevatedButton(
        onPressed: canVerify ? _verifyOtp : null,
        style: ElevatedButton.styleFrom(
          elevation: 0,
          backgroundColor: const Color(0xFF1677FF),
          disabledBackgroundColor: Colors.white.withValues(
            alpha: 0.07,
          ),
          foregroundColor: Colors.white,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(17),
          ),
        ),
        child: _isVerifying
            ? const SizedBox(
                width: 23,
                height: 23,
                child: CircularProgressIndicator(
                  strokeWidth: 2.5,
                  valueColor:
                      AlwaysStoppedAnimation<Color>(
                    Colors.white,
                  ),
                ),
              )
            : const Row(
                mainAxisAlignment:
                    MainAxisAlignment.center,
                children: [
                  Text(
                    'Verify Code',
                    style: TextStyle(
                      fontSize: 15.5,
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                  SizedBox(width: 9),
                  Icon(
                    Icons.arrow_forward_rounded,
                    size: 20,
                  ),
                ],
              ),
      ),
    );
  }
  Future<void> _verifyOtp() async {
  final String otp = _otpController.text.trim();

  if (otp.length != 6 ||
      _remainingSeconds <= 0 ||
      _isVerifying) {
    return;
  }

  setState(() {
    _isVerifying = true;
  });

  try {
    final response = await _authService.verifyOtp(
      tempAuthSessionId: widget.tempAuthSessionId,
      otp: otp,
    );

    if (!mounted) {
      return;
    }

    if (response.status != 'SUCCESS') {
      throw Exception(
        'OTP verification was not successful.',
      );
    }

    if (response.accessToken.isEmpty ||
        response.refreshToken.isEmpty) {
      throw Exception(
        'Authentication tokens were not returned.',
      );
    }

    await _storage.saveTokens(
      accessToken: response.accessToken,
      refreshToken: response.refreshToken,
    );

    if (!mounted) {
      return;
    }

    await Navigator.of(context).pushReplacement(
      MaterialPageRoute(
        builder: (_) => WelcomeScreen(
          accessToken: response.accessToken,
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
        _isVerifying = false;
      });
    }
  }
}
}

class _SecurityGridPainter extends CustomPainter {
  final double progress;

  const _SecurityGridPainter({
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
        alpha: 0.025,
      );

    const spacing = 46.0;

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
      ..color = const Color(0xFF4DA3FF).withValues(
        alpha: 0.06,
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
          Offset(
            x,
            y + (shift * 0.12),
          ),
          1.5,
          nodePaint,
        );
      }
    }
  }

  @override
  bool shouldRepaint(
    covariant _SecurityGridPainter oldDelegate,
  ) {
    return oldDelegate.progress != progress;
  }
}
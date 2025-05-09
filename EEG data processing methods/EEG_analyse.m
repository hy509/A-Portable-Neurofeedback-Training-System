clc;
clear;
% clear all;

SampleFre = 512;            % Sampling frequencies

%% 导入数据
data = load('');
eeg_sig = data;
SignalLen = length(eeg_sig);
t = (1:SignalLen) / SampleFre;
figure;
plot(t, eeg_sig,'-b');
xlabel("Time (s)");ylabel("Output (mV)");	% Raw signal
grid on;

%% Decompose and reconstruct signals 
sc = 9;  % 256 / 2`9 = 0.5
wpt21 = wpdec(eeg_sig, sc, 'db6', 'shannon');	% Wavelet packet decomposition signal

% Reconstruct signal
base_line = wprcoef(wpt21, [9 0]);
close_delta1 = wprcoef(wpt21, [6 0]) - base_line;  % delta(0.5-3.5 Hz)               
close_theta1 = wprcoef(wpt21, [6 1]);  % theta(4-8),
close_alpha1 = wprcoef(wpt21, [6 2])+ wprcoef(wpt21, [7 6]);  % alpha(8-13 Hz)
close_beta1 = wprcoef(wpt21, [4 1])+ wprcoef(wpt21, [7 7]) - wprcoef(wpt21, [7 15]);   % beta(18-30 Hz)
close_gamma1 = wprcoef(wpt21, [3 1]) + wprcoef(wpt21, [7 15]);  % gamma(30-48 Hz)

% % Draw the reconstructed signal
figure;
subplot(2,3,1); plot(t, eeg_sig);grid on;
xlabel("Time (s)");ylabel("Output (mV)"); title("Raw EEG signal");
subplot(2,3,2); plot(t, close_delta1);grid on;
xlabel("Time (s)");ylabel("Output (mV)"); title("Delta wave");
subplot(2,3,3); plot(t, close_theta1); grid on;
xlabel("Time (s)");ylabel("Output (mV)"); title("Theta wave");
subplot(2,3,4); plot(t, close_alpha1); grid on;
xlabel("Time (s)");ylabel("Output (mV)"); title("Alpha wave");
subplot(2,3,5); plot(t, close_beta1); grid on;
xlabel("Time (s)");ylabel("Output (mV)"); title("Beta wave");
subplot(2,3,6); plot(t, close_gamma1); grid on;
xlabel("Time (s)");ylabel("Output (mV)"); title("Gamma wave");
% 
% 
% The frequency spectrum of the reconstructed signal
[Frequence10,FFTAmplitude10] = compute_fft( eeg_sig, SampleFre, SignalLen );
[Frequence11,FFTAmplitude11] = compute_fft( close_delta1, SampleFre, SignalLen );
[Frequence12,FFTAmplitude12] = compute_fft( close_theta1, SampleFre, SignalLen );
[Frequence13,FFTAmplitude13] = compute_fft( close_alpha1, SampleFre, SignalLen );
[Frequence14,FFTAmplitude14] = compute_fft( close_beta1, SampleFre, SignalLen );
[Frequence15,FFTAmplitude15] = compute_fft( close_gamma1, SampleFre, SignalLen );
% Calculate the energy of the signal
energy_f11=sum((abs(FFTAmplitude11)));
energy_f12=sum((abs(FFTAmplitude12)));
energy_f13=sum((abs(FFTAmplitude13)));
energy_f14=sum((abs(FFTAmplitude14)));
energy_f15=sum((abs(FFTAmplitude15)));

% Calculate the TBR
TBR = energy_f12/energy_f14;







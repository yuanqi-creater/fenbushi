import React, { useState, useEffect } from 'react';
import { 
  Card, 
  Steps, 
  Button, 
  Radio, 
  Row, 
  Col, 
  Statistic, 
  Result,
  Space,
  message,
  Modal
} from 'antd';
import { useParams, useNavigate } from 'react-router-dom';
import {
  WalletOutlined,
  CheckCircleOutlined,
  LoadingOutlined,
  QrcodeOutlined
} from '@ant-design/icons';
import { QRCodeCanvas } from 'qrcode.react';

const { Step } = Steps;

const Payment = () => {
  const { orderId } = useParams();
  const navigate = useNavigate();
  const [currentStep, setCurrentStep] = useState(0);
  const [paymentMethod, setPaymentMethod] = useState('alipay');
  const [orderInfo, setOrderInfo] = useState(null);
  const [loading, setLoading] = useState(true);
  const [qrCode, setQrCode] = useState('');
  const [paymentStatus, setPaymentStatus] = useState('pending'); // pending, success, failed
  const [countdown, setCountdown] = useState(900); // 15分钟支付倒计时

  // 获取订单信息
  useEffect(() => {
    fetchOrderInfo();
  }, [orderId]);

  // 倒计时
  useEffect(() => {
    if (countdown > 0 && paymentStatus === 'pending') {
      const timer = setInterval(() => {
        setCountdown(prev => prev - 1);
      }, 1000);
      return () => clearInterval(timer);
    } else if (countdown === 0) {
      message.error('支付超时，订单已取消');
      navigate('/user/orders');
    }
  }, [countdown, paymentStatus]);

  // 轮询支付状态
  useEffect(() => {
    if (paymentStatus === 'pending' && qrCode) {
      const timer = setInterval(checkPaymentStatus, 3000);
      return () => clearInterval(timer);
    }
  }, [paymentStatus, qrCode]);

  const fetchOrderInfo = async () => {
    try {
      // TODO: 替换为实际API调用
      const mockOrderInfo = {
        orderId,
        eventName: 'Taylor Swift Eras Tour',
        totalAmount: 2888.00,
        tickets: [
          { type: 'VIP', quantity: 1, price: 2888.00 }
        ],
        expireTime: new Date(Date.now() + 900000).toISOString()
      };
      setOrderInfo(mockOrderInfo);
    } catch (error) {
      message.error('获取订单信息失败');
    } finally {
      setLoading(false);
    }
  };

  const handlePaymentMethodChange = (e) => {
    setPaymentMethod(e.target.value);
  };

  const generateQRCode = async () => {
    try {
      // TODO: 替换为实际API调用
      const mockQrCode = 'https://example.com/pay/' + orderId;
      setQrCode(mockQrCode);
      setCurrentStep(1);
    } catch (error) {
      message.error('生成支付二维码失败');
    }
  };

  const checkPaymentStatus = async () => {
    try {
      // TODO: 替换为实际API调用
      // 模拟支付成功
      if (Math.random() > 0.9) {
        setPaymentStatus('success');
        setCurrentStep(2);
      }
    } catch (error) {
      console.error('检查支付状态失败:', error);
    }
  };

  const formatTime = (seconds) => {
    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = seconds % 60;
    return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`;
  };

  return (
    <div style={{ maxWidth: 800, margin: '0 auto', padding: '24px' }}>
      <Steps current={currentStep}>
        <Step title="选择支付方式" icon={<WalletOutlined />} />
        <Step title="扫码支付" icon={currentStep === 1 ? <LoadingOutlined /> : <QrcodeOutlined />} />
        <Step title="支付完成" icon={<CheckCircleOutlined />} />
      </Steps>

      <Card style={{ marginTop: 24 }}>
        {currentStep === 0 && (
          <>
            <Row gutter={24}>
              <Col span={16}>
                <h3>订单信息</h3>
                <p>演出：{orderInfo?.eventName}</p>
                <p>订单号：{orderId}</p>
                {orderInfo?.tickets.map((ticket, index) => (
                  <p key={index}>
                    {ticket.type}: {ticket.quantity}张 x ¥{ticket.price}
                  </p>
                ))}
              </Col>
              <Col span={8}>
                <Statistic
                  title="支付金额"
                  value={orderInfo?.totalAmount}
                  prefix="¥"
                  precision={2}
                  style={{ marginBottom: 24 }}
                />
                <Statistic
                  title="支付倒计时"
                  value={formatTime(countdown)}
                  suffix="后订单自动取消"
                />
              </Col>
            </Row>
            <div style={{ marginTop: 24 }}>
              <h3>选择支付方式</h3>
              <Radio.Group onChange={handlePaymentMethodChange} value={paymentMethod}>
                <Space direction="vertical">
                  <Radio value="alipay">支付宝</Radio>
                  <Radio value="wechat">微信支付</Radio>
                </Space>
              </Radio.Group>
            </div>
            <Button 
              type="primary" 
              size="large" 
              style={{ marginTop: 24 }}
              onClick={generateQRCode}
            >
              确认支付
            </Button>
          </>
        )}

        {currentStep === 1 && (
          <div style={{ textAlign: 'center' }}>
            <h3>请使用{paymentMethod === 'alipay' ? '支付宝' : '微信'}扫码支付</h3>
            <div style={{ marginTop: 24, marginBottom: 24 }}>
              <QRCodeCanvas value={qrCode} size={200} />
            </div>
            <Statistic
              title="支付倒计时"
              value={formatTime(countdown)}
              suffix="后订单自动取消"
            />
          </div>
        )}

        {currentStep === 2 && (
          <Result
            status="success"
            title="支付成功"
            subTitle={`订单号: ${orderId}`}
            extra={[
              <Button 
                type="primary" 
                key="orders"
                onClick={() => navigate('/user/orders')}
              >
                查看订单
              </Button>,
              <Button 
                key="home"
                onClick={() => navigate('/')}
              >
                返回首页
              </Button>,
            ]}
          />
        )}
      </Card>
    </div>
  );
};

export default Payment; 
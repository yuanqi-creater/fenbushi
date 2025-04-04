import React, { useState, useEffect } from 'react';
import {
  Card,
  Form,
  Input,
  Button,
  Radio,
  Alert,
  Steps,
  Result,
  Descriptions,
  Space,
  message,
  Modal
} from 'antd';
import { useParams, useNavigate } from 'react-router-dom';
import {
  ExclamationCircleOutlined,
  CheckCircleOutlined,
  LoadingOutlined
} from '@ant-design/icons';

const { Step } = Steps;
const { TextArea } = Input;

const RefundRequest = () => {
  const { orderId } = useParams();
  const navigate = useNavigate();
  const [form] = Form.useForm();
  const [currentStep, setCurrentStep] = useState(0);
  const [orderInfo, setOrderInfo] = useState(null);
  const [loading, setLoading] = useState(true);
  const [refundFee, setRefundFee] = useState(0);
  const [refundAmount, setRefundAmount] = useState(0);

  // 获取订单信息
  useEffect(() => {
    fetchOrderInfo();
  }, [orderId]);

  const fetchOrderInfo = async () => {
    try {
      // TODO: 替换为实际API调用
      const mockOrderInfo = {
        orderId,
        eventName: 'Taylor Swift Eras Tour',
        eventTime: '2024-06-15 19:30:00',
        totalAmount: 2888.00,
        tickets: [
          { type: 'VIP', quantity: 1, price: 2888.00 }
        ],
        purchaseTime: '2024-03-15 14:30:00'
      };
      setOrderInfo(mockOrderInfo);
      
      // 计算退款手续费和退款金额
      const fee = mockOrderInfo.totalAmount * 0.1; // 10%手续费
      setRefundFee(fee);
      setRefundAmount(mockOrderInfo.totalAmount - fee);
    } catch (error) {
      message.error('获取订单信息失败');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (values) => {
    Modal.confirm({
      title: '确认提交退票申请',
      icon: <ExclamationCircleOutlined />,
      content: (
        <div>
          <p>退款金额：¥{refundAmount.toFixed(2)}</p>
          <p>手续费：¥{refundFee.toFixed(2)}</p>
          <p>退款原因：{values.reason}</p>
        </div>
      ),
      onOk: async () => {
        try {
          // TODO: 替换为实际API调用
          await new Promise(resolve => setTimeout(resolve, 1000));
          setCurrentStep(2);
          message.success('退票申请提交成功');
        } catch (error) {
          message.error('退票申请提交失败');
        }
      }
    });
  };

  const renderOrderInfo = () => (
    <Descriptions title="订单信息" bordered>
      <Descriptions.Item label="演出" span={3}>
        {orderInfo?.eventName}
      </Descriptions.Item>
      <Descriptions.Item label="演出时间" span={3}>
        {orderInfo?.eventTime}
      </Descriptions.Item>
      <Descriptions.Item label="订单号" span={3}>
        {orderId}
      </Descriptions.Item>
      <Descriptions.Item label="购票时间" span={3}>
        {orderInfo?.purchaseTime}
      </Descriptions.Item>
      <Descriptions.Item label="票品" span={3}>
        {orderInfo?.tickets.map((ticket, index) => (
          <div key={index}>
            {ticket.type}: {ticket.quantity}张 x ¥{ticket.price}
          </div>
        ))}
      </Descriptions.Item>
      <Descriptions.Item label="订单金额" span={3}>
        ¥{orderInfo?.totalAmount.toFixed(2)}
      </Descriptions.Item>
    </Descriptions>
  );

  return (
    <div style={{ maxWidth: 800, margin: '0 auto', padding: '24px' }}>
      <Steps current={currentStep}>
        <Step title="确认订单信息" icon={<ExclamationCircleOutlined />} />
        <Step title="填写退票信息" icon={currentStep === 1 ? <LoadingOutlined /> : null} />
        <Step title="提交成功" icon={<CheckCircleOutlined />} />
      </Steps>

      <Card style={{ marginTop: 24 }}>
        {currentStep === 0 && (
          <>
            {renderOrderInfo()}
            <Alert
              message="退票须知"
              description={
                <ul>
                  <li>距离演出开始前48小时以上可申请退票</li>
                  <li>退票将收取票面价格10%的手续费</li>
                  <li>退款将原路返回至支付账户</li>
                  <li>退票申请提交后无法撤销</li>
                </ul>
              }
              type="warning"
              showIcon
              style={{ marginTop: 24, marginBottom: 24 }}
            />
            <div style={{ textAlign: 'center' }}>
              <Space>
                <Button onClick={() => navigate('/user/orders')}>取消</Button>
                <Button type="primary" onClick={() => setCurrentStep(1)}>
                  继续退票
                </Button>
              </Space>
            </div>
          </>
        )}

        {currentStep === 1 && (
          <Form form={form} onFinish={handleSubmit} layout="vertical">
            {renderOrderInfo()}
            
            <Alert
              message="退款金额明细"
              description={
                <div>
                  <p>票面金额：¥{orderInfo?.totalAmount.toFixed(2)}</p>
                  <p>手续费(10%)：¥{refundFee.toFixed(2)}</p>
                  <p>实际退款：¥{refundAmount.toFixed(2)}</p>
                </div>
              }
              type="info"
              showIcon
              style={{ marginTop: 24, marginBottom: 24 }}
            />

            <Form.Item
              name="reason"
              label="退票原因"
              rules={[{ required: true, message: '请选择退票原因' }]}
            >
              <Radio.Group>
                <Space direction="vertical">
                  <Radio value="schedule_conflict">档期冲突</Radio>
                  <Radio value="cannot_attend">无法参加</Radio>
                  <Radio value="bought_wrong">购买错误</Radio>
                  <Radio value="other">其他原因</Radio>
                </Space>
              </Radio.Group>
            </Form.Item>

            <Form.Item
              name="description"
              label="补充说明"
              rules={[{ max: 200, message: '补充说明不能超过200字' }]}
            >
              <TextArea rows={4} placeholder="请输入补充说明（选填）" maxLength={200} />
            </Form.Item>

            <Form.Item>
              <Space>
                <Button onClick={() => setCurrentStep(0)}>上一步</Button>
                <Button type="primary" htmlType="submit">
                  提交申请
                </Button>
              </Space>
            </Form.Item>
          </Form>
        )}

        {currentStep === 2 && (
          <Result
            status="success"
            title="退票申请提交成功"
            subTitle={
              <div>
                <p>订单号: {orderId}</p>
                <p>预计退款金额: ¥{refundAmount.toFixed(2)}</p>
                <p>退款将在1-3个工作日内原路返回</p>
              </div>
            }
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

export default RefundRequest; 
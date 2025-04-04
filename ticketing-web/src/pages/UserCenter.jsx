import React, { useState, useEffect } from 'react';
import { 
  Layout, 
  Menu, 
  Card, 
  Table, 
  Tag, 
  Button, 
  Modal, 
  Form, 
  Input,
  message,
  Tabs,
  Statistic,
  Row,
  Col,
  Space
} from 'antd';
import {
  UserOutlined,
  ShoppingOutlined,
  WalletOutlined,
  SettingOutlined
} from '@ant-design/icons';
import moment from 'moment';

const { Sider, Content } = Layout;
const { TabPane } = Tabs;

const UserCenter = () => {
  const [selectedKey, setSelectedKey] = useState('orders');
  const [orders, setOrders] = useState([]);
  const [refunds, setRefunds] = useState([]);
  const [loading, setLoading] = useState(false);
  const [userInfo, setUserInfo] = useState(null);
  const [passwordModal, setPasswordModal] = useState(false);

  // 获取订单列表
  useEffect(() => {
    fetchOrders();
  }, []);

  const fetchOrders = async () => {
    setLoading(true);
    try {
      // TODO: 替换为实际API调用
      const mockOrders = [
        {
          orderId: 1,
          eventName: 'Taylor Swift Eras Tour',
          orderTime: '2024-03-15 14:30:00',
          amount: 2888.00,
          status: 'PAID',
          tickets: [
            { type: 'VIP', quantity: 1 }
          ]
        }
      ];
      setOrders(mockOrders);
    } catch (error) {
      message.error('获取订单列表失败');
    } finally {
      setLoading(false);
    }
  };

  // 订单列表列定义
  const orderColumns = [
    {
      title: '订单号',
      dataIndex: 'orderId',
      key: 'orderId',
    },
    {
      title: '演出',
      dataIndex: 'eventName',
      key: 'eventName',
    },
    {
      title: '下单时间',
      dataIndex: 'orderTime',
      key: 'orderTime',
      render: (text) => moment(text).format('YYYY-MM-DD HH:mm:ss'),
    },
    {
      title: '金额',
      dataIndex: 'amount',
      key: 'amount',
      render: (text) => `¥${text.toFixed(2)}`,
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status) => {
        const statusMap = {
          'PENDING_PAYMENT': { color: 'orange', text: '待支付' },
          'PAID': { color: 'green', text: '已支付' },
          'COMPLETED': { color: 'blue', text: '已完成' },
          'CANCELLED': { color: 'red', text: '已取消' },
          'REFUNDING': { color: 'purple', text: '退款中' },
          'REFUNDED': { color: 'gray', text: '已退款' }
        };
        const { color, text } = statusMap[status] || statusMap.COMPLETED;
        return <Tag color={color}>{text}</Tag>;
      },
    },
    {
      title: '操作',
      key: 'action',
      render: (_, record) => (
        <Space>
          <Button size="small" onClick={() => handleViewOrder(record.orderId)}>
            查看
          </Button>
          {record.status === 'PAID' && (
            <Button 
              size="small" 
              type="primary" 
              danger
              onClick={() => handleRefund(record.orderId)}
            >
              申请退票
            </Button>
          )}
        </Space>
      ),
    },
  ];

  // 查看订单详情
  const handleViewOrder = (orderId) => {
    // TODO: 实现查看订单详情
    console.log('查看订单:', orderId);
  };

  // 申请退票
  const handleRefund = (orderId) => {
    Modal.confirm({
      title: '申请退票',
      content: '确定要申请退票吗？退票可能会收取手续费。',
      onOk: async () => {
        try {
          // TODO: 实现退票逻辑
          message.success('退票申请已提交');
        } catch (error) {
          message.error('退票申请失败');
        }
      },
    });
  };

  // 修改密码
  const handleChangePassword = async (values) => {
    try {
      // TODO: 实现修改密码
      message.success('密码修改成功');
      setPasswordModal(false);
    } catch (error) {
      message.error('密码修改失败');
    }
  };

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider width={200} theme="light">
        <Menu
          mode="inline"
          selectedKeys={[selectedKey]}
          onSelect={({ key }) => setSelectedKey(key)}
          style={{ height: '100%' }}
        >
          <Menu.Item key="orders" icon={<ShoppingOutlined />}>
            我的订单
          </Menu.Item>
          <Menu.Item key="refunds" icon={<WalletOutlined />}>
            退票记录
          </Menu.Item>
          <Menu.Item key="profile" icon={<UserOutlined />}>
            个人信息
          </Menu.Item>
          <Menu.Item key="settings" icon={<SettingOutlined />}>
            账号设置
          </Menu.Item>
        </Menu>
      </Sider>
      <Content style={{ padding: '24px', minHeight: 280 }}>
        {selectedKey === 'orders' && (
          <Card title="我的订单">
            <Table
              columns={orderColumns}
              dataSource={orders}
              loading={loading}
              rowKey="orderId"
            />
          </Card>
        )}

        {selectedKey === 'refunds' && (
          <Card title="退票记录">
            <Table
              columns={[
                {
                  title: '订单号',
                  dataIndex: 'orderId',
                },
                {
                  title: '申请时间',
                  dataIndex: 'applyTime',
                  render: (text) => moment(text).format('YYYY-MM-DD HH:mm:ss'),
                },
                {
                  title: '退款金额',
                  dataIndex: 'refundAmount',
                  render: (text) => `¥${text.toFixed(2)}`,
                },
                {
                  title: '状态',
                  dataIndex: 'status',
                  render: (status) => {
                    const statusMap = {
                      'PENDING_REVIEW': { color: 'orange', text: '待审核' },
                      'APPROVED': { color: 'green', text: '已批准' },
                      'REJECTED': { color: 'red', text: '已拒绝' },
                      'COMPLETED': { color: 'blue', text: '已完成' }
                    };
                    const { color, text } = statusMap[status] || {};
                    return <Tag color={color}>{text}</Tag>;
                  },
                }
              ]}
              dataSource={refunds}
              loading={loading}
              rowKey="refundId"
            />
          </Card>
        )}

        {selectedKey === 'profile' && (
          <Card title="个人信息">
            <Row gutter={24}>
              <Col span={8}>
                <Card>
                  <Statistic title="总订单" value={orders.length} />
                </Card>
              </Col>
              <Col span={8}>
                <Card>
                  <Statistic 
                    title="消费金额" 
                    value={orders.reduce((sum, order) => sum + order.amount, 0)} 
                    prefix="¥" 
                    precision={2} 
                  />
                </Card>
              </Col>
              <Col span={8}>
                <Card>
                  <Statistic title="退票率" value={15} suffix="%" />
                </Card>
              </Col>
            </Row>
          </Card>
        )}

        {selectedKey === 'settings' && (
          <Card title="账号设置">
            <Button type="primary" onClick={() => setPasswordModal(true)}>
              修改密码
            </Button>
          </Card>
        )}
      </Content>

      <Modal
        title="修改密码"
        open={passwordModal}
        onCancel={() => setPasswordModal(false)}
        footer={null}
      >
        <Form onFinish={handleChangePassword}>
          <Form.Item
            name="oldPassword"
            label="原密码"
            rules={[{ required: true, message: '请输入原密码' }]}
          >
            <Input.Password />
          </Form.Item>
          <Form.Item
            name="newPassword"
            label="新密码"
            rules={[{ required: true, message: '请输入新密码' }]}
          >
            <Input.Password />
          </Form.Item>
          <Form.Item
            name="confirmPassword"
            label="确认密码"
            rules={[
              { required: true, message: '请确认新密码' },
              ({ getFieldValue }) => ({
                validator(_, value) {
                  if (!value || getFieldValue('newPassword') === value) {
                    return Promise.resolve();
                  }
                  return Promise.reject(new Error('两次输入的密码不一致'));
                },
              }),
            ]}
          >
            <Input.Password />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit">
              确认修改
            </Button>
          </Form.Item>
        </Form>
      </Modal>
    </Layout>
  );
};

export default UserCenter; 
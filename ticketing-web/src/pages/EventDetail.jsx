import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { 
  Card, 
  Row, 
  Col, 
  Button, 
  Space, 
  Tag, 
  Divider, 
  message,
  Modal,
  InputNumber,
  Steps
} from 'antd';
import { 
  CalendarOutlined, 
  EnvironmentOutlined, 
  ShoppingCartOutlined,
  UserOutlined
} from '@ant-design/icons';
import moment from 'moment';

const { Step } = Steps;

const EventDetail = () => {
  const { eventId } = useParams();
  const [event, setEvent] = useState(null);
  const [loading, setLoading] = useState(false);
  const [selectedType, setSelectedType] = useState(null);
  const [quantity, setQuantity] = useState(1);
  const [modalVisible, setModalVisible] = useState(false);
  const [currentStep, setCurrentStep] = useState(0);
  const [selectedSeats, setSelectedSeats] = useState([]);

  // 获取活动详情
  useEffect(() => {
    fetchEventDetail();
  }, [eventId]);

  const fetchEventDetail = async () => {
    setLoading(true);
    try {
      // TODO: 替换为实际的API调用
      const mockEvent = {
        eventId: 1,
        eventName: 'Taylor Swift Eras Tour',
        eventTime: '2024-05-01 19:30:00',
        venueName: '国家体育场鸟巢',
        totalSeats: 80000,
        remainingSeats: 5000,
        saleStatus: 'ON_SALE',
        ticketTypes: [
          { typeId: 1, typeName: 'VIP', price: 288800, remainingQuantity: 100, limitPerPerson: 2 },
          { typeId: 2, typeName: '看台A', price: 188800, remainingQuantity: 2000, limitPerPerson: 4 },
          { typeId: 3, typeName: '看台B', price: 88800, remainingQuantity: 2900, limitPerPerson: 4 }
        ],
        description: '泰勒·斯威夫特"时代"巡回演唱会北京站...',
        notice: '1. 每人每场限购4张\n2. 观众需在演出前30分钟入场\n3. 门票不可退换',
        seatMap: 'https://source.unsplash.com/1600x900/?stadium'
      };
      setEvent(mockEvent);
    } catch (error) {
      message.error('获取活动详情失败');
    } finally {
      setLoading(false);
    }
  };

  // 选择票价类型
  const handleSelectType = (type) => {
    setSelectedType(type);
    setQuantity(1);
    setModalVisible(true);
  };

  // 渲染座位图
  const renderSeatMap = () => {
    // TODO: 实现真实的座位选择逻辑
    return (
      <div style={{ position: 'relative' }}>
        <img 
          src={event?.seatMap} 
          alt="座位图" 
          style={{ width: '100%', height: 'auto' }}
        />
        <div style={{ 
          position: 'absolute', 
          top: '50%', 
          left: '50%', 
          transform: 'translate(-50%, -50%)',
          color: 'white',
          background: 'rgba(0,0,0,0.5)',
          padding: '20px'
        }}>
          座位选择功能开发中...
        </div>
      </div>
    );
  };

  // 处理购票
  const handlePurchase = async () => {
    try {
      // TODO: 实现实际的购票逻辑
      message.success('订单创建成功！');
      setModalVisible(false);
    } catch (error) {
      message.error('购票失败');
    }
  };

  if (!event) {
    return null;
  }

  return (
    <div style={{ padding: '24px' }}>
      <Card loading={loading}>
        <Row gutter={24}>
          <Col span={8}>
            <img
              alt={event.eventName}
              src={`https://source.unsplash.com/800x600/?concert&sig=${event.eventId}`}
              style={{ width: '100%', borderRadius: '8px' }}
            />
          </Col>
          <Col span={16}>
            <Space direction="vertical" size="large" style={{ width: '100%' }}>
              <Space>
                <h1>{event.eventName}</h1>
                <Tag color={event.saleStatus === 'ON_SALE' ? 'green' : 'red'}>
                  {event.saleStatus === 'ON_SALE' ? '销售中' : '已售罄'}
                </Tag>
              </Space>
              
              <Space direction="vertical">
                <div><CalendarOutlined /> 演出时间：{moment(event.eventTime).format('YYYY-MM-DD HH:mm')}</div>
                <div><EnvironmentOutlined /> 演出场馆：{event.venueName}</div>
                <div><UserOutlined /> 剩余座位：{event.remainingSeats}/{event.totalSeats}</div>
              </Space>

              <Divider />

              <div>
                <h3>票价信息</h3>
                <Row gutter={[16, 16]}>
                  {event.ticketTypes.map(type => (
                    <Col span={8} key={type.typeId}>
                      <Card>
                        <div>{type.typeName}</div>
                        <div style={{ color: '#f5222d', fontSize: '20px', margin: '8px 0' }}>
                          ¥{(type.price / 100).toFixed(2)}
                        </div>
                        <div>剩余：{type.remainingQuantity}张</div>
                        <div>限购：{type.limitPerPerson}张/人</div>
                        <Button
                          type="primary"
                          block
                          style={{ marginTop: '8px' }}
                          disabled={type.remainingQuantity === 0}
                          onClick={() => handleSelectType(type)}
                        >
                          选择
                        </Button>
                      </Card>
                    </Col>
                  ))}
                </Row>
              </div>

              <Divider />

              <div>
                <h3>演出介绍</h3>
                <p>{event.description}</p>
              </div>

              <div>
                <h3>购票须知</h3>
                <pre style={{ whiteSpace: 'pre-wrap' }}>{event.notice}</pre>
              </div>
            </Space>
          </Col>
        </Row>
      </Card>

      <Modal
        title="购票确认"
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        footer={null}
        width={800}
      >
        <Steps current={currentStep} style={{ marginBottom: '24px' }}>
          <Step title="选择数量" />
          <Step title="选择座位" />
          <Step title="确认订单" />
        </Steps>

        {currentStep === 0 && (
          <div>
            <Space direction="vertical" style={{ width: '100%' }}>
              <div>
                票价类型：{selectedType?.typeName}
                <Tag color="red" style={{ marginLeft: '8px' }}>
                  ¥{selectedType ? (selectedType.price / 100).toFixed(2) : 0}
                </Tag>
              </div>
              <div>
                购买数量：
                <InputNumber
                  min={1}
                  max={selectedType?.limitPerPerson || 1}
                  value={quantity}
                  onChange={setQuantity}
                />
                <span style={{ marginLeft: '8px', color: '#888' }}>
                  (限购{selectedType?.limitPerPerson}张)
                </span>
              </div>
              <div>
                总价：
                <span style={{ color: '#f5222d', fontSize: '20px' }}>
                  ¥{selectedType ? ((selectedType.price * quantity) / 100).toFixed(2) : 0}
                </span>
              </div>
              <Button 
                type="primary" 
                block
                onClick={() => setCurrentStep(1)}
              >
                下一步
              </Button>
            </Space>
          </div>
        )}

        {currentStep === 1 && (
          <div>
            {renderSeatMap()}
            <div style={{ marginTop: '24px', textAlign: 'right' }}>
              <Space>
                <Button onClick={() => setCurrentStep(0)}>上一步</Button>
                <Button type="primary" onClick={() => setCurrentStep(2)}>
                  下一步
                </Button>
              </Space>
            </div>
          </div>
        )}

        {currentStep === 2 && (
          <div>
            <h3>订单确认</h3>
            <div>
              <p>演出：{event.eventName}</p>
              <p>时间：{moment(event.eventTime).format('YYYY-MM-DD HH:mm')}</p>
              <p>场馆：{event.venueName}</p>
              <p>票价：{selectedType?.typeName} ¥{selectedType ? (selectedType.price / 100).toFixed(2) : 0}</p>
              <p>数量：{quantity}张</p>
              <p>总价：¥{selectedType ? ((selectedType.price * quantity) / 100).toFixed(2) : 0}</p>
            </div>
            <div style={{ marginTop: '24px', textAlign: 'right' }}>
              <Space>
                <Button onClick={() => setCurrentStep(1)}>上一步</Button>
                <Button type="primary" onClick={handlePurchase}>
                  确认购买
                </Button>
              </Space>
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
};

export default EventDetail; 
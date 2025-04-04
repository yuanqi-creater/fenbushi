import React, { useState, useEffect } from 'react';
import { Card, List, Tag, Button, Space, message } from 'antd';
import { CalendarOutlined, EnvironmentOutlined, ShoppingCartOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import moment from 'moment';

const EventList = () => {
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  // 获取活动列表
  useEffect(() => {
    fetchEvents();
  }, []);

  const fetchEvents = async () => {
    setLoading(true);
    try {
      // TODO: 替换为实际的API调用
      const mockEvents = [
        {
          eventId: 1,
          eventName: 'Taylor Swift Eras Tour',
          eventTime: '2024-05-01 19:30:00',
          venueName: '国家体育场鸟巢',
          totalSeats: 80000,
          remainingSeats: 5000,
          saleStatus: 'ON_SALE',
          ticketTypes: [
            { typeId: 1, typeName: 'VIP', price: 288800, remainingQuantity: 100 },
            { typeId: 2, typeName: '看台A', price: 188800, remainingQuantity: 2000 },
            { typeId: 3, typeName: '看台B', price: 88800, remainingQuantity: 2900 }
          ]
        },
        {
          eventId: 2,
          eventName: '周杰伦 2024巡回演唱会',
          eventTime: '2024-06-15 19:30:00',
          venueName: '上海梅赛德斯奔驰文化中心',
          totalSeats: 18000,
          remainingSeats: 0,
          saleStatus: 'SOLD_OUT',
          ticketTypes: [
            { typeId: 4, typeName: 'VIP', price: 188800, remainingQuantity: 0 },
            { typeId: 5, typeName: '看台', price: 88800, remainingQuantity: 0 }
          ]
        }
      ];
      setEvents(mockEvents);
    } catch (error) {
      message.error('获取活动列表失败');
    } finally {
      setLoading(false);
    }
  };

  // 获取状态标签
  const getStatusTag = (status) => {
    const statusMap = {
      PENDING: { color: 'orange', text: '未开始' },
      ON_SALE: { color: 'green', text: '销售中' },
      SOLD_OUT: { color: 'red', text: '已售罄' },
      CLOSED: { color: 'default', text: '已结束' }
    };
    const { color, text } = statusMap[status] || statusMap.CLOSED;
    return <Tag color={color}>{text}</Tag>;
  };

  // 渲染票价信息
  const renderTicketTypes = (ticketTypes) => {
    return (
      <Space direction="vertical">
        {ticketTypes.map(type => (
          <div key={type.typeId}>
            {type.typeName}: ¥{(type.price / 100).toFixed(2)}
            {type.remainingQuantity > 0 ? 
              ` (剩余${type.remainingQuantity}张)` : 
              ' (售罄)'}
          </div>
        ))}
      </Space>
    );
  };

  return (
    <div style={{ padding: '24px' }}>
      <List
        grid={{ gutter: 16, column: 3 }}
        dataSource={events}
        loading={loading}
        renderItem={event => (
          <List.Item>
            <Card
              hoverable
              cover={
                <img
                  alt={event.eventName}
                  src={`https://source.unsplash.com/800x600/?concert&sig=${event.eventId}`}
                  style={{ height: 200, objectFit: 'cover' }}
                />
              }
              actions={[
                <Button 
                  type="primary" 
                  icon={<ShoppingCartOutlined />}
                  disabled={event.saleStatus !== 'ON_SALE'}
                  onClick={() => navigate(`/event/${event.eventId}`)}
                >
                  立即购票
                </Button>
              ]}
            >
              <Card.Meta
                title={
                  <Space>
                    {event.eventName}
                    {getStatusTag(event.saleStatus)}
                  </Space>
                }
                description={
                  <Space direction="vertical">
                    <div>
                      <CalendarOutlined /> {moment(event.eventTime).format('YYYY-MM-DD HH:mm')}
                    </div>
                    <div>
                      <EnvironmentOutlined /> {event.venueName}
                    </div>
                    <div>
                      剩余座位：{event.remainingSeats}/{event.totalSeats}
                    </div>
                    {renderTicketTypes(event.ticketTypes)}
                  </Space>
                }
              />
            </Card>
          </List.Item>
        )}
      />
    </div>
  );
};

export default EventList; 